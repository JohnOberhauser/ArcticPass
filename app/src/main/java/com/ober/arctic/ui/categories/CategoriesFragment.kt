package com.ober.arctic.ui.categories

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.OnClick
import com.google.gson.Gson
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.BundleConstants
import com.ober.arctic.util.FileUtil
import com.ober.arctic.util.security.Encryption
import com.ober.arctic.util.security.KeyManager
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.android.synthetic.main.fragment_landing.*
import java.util.*
import javax.inject.Inject
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.ober.arctic.MainActivity.Companion.READ_REQUEST_CODE
import com.ober.arctic.OnImportFileListener
import com.ober.arctic.data.model.*
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.TypeUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.GeneralSecurityException
import java.util.concurrent.CountDownLatch


class CategoriesFragment : BaseFragment(), CategoryRecyclerAdapter.CategoryClickedListener, OnImportFileListener {

    @Inject
    lateinit var encryption: Encryption

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var keyManager: KeyManager

    @Inject
    lateinit var appExecutors: AppExecutors

    private lateinit var dataViewModel: DataViewModel

    private var categoryAdapter: CategoryRecyclerAdapter? = null

    private var categoryCollection: CategoryCollection? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        dataViewModel = ViewModelProviders.of(this, viewModelFactory)[DataViewModel::class.java]
        return setAndBindContentView(inflater, container!!, R.layout.fragment_landing)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObserver()
        setupDrawerClickListeners()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryRecyclerAdapter(this)
        category_recycler_view.adapter = categoryAdapter
        category_recycler_view.layoutManager = LinearLayoutManager(context)
    }

    private fun setupObserver() {
        dataViewModel.domainCollectionLiveData.observe(this, Observer {
            progress_bar.visibility = View.GONE
            categoryAdapter?.categories = it.categories
            categoryCollection = CategoryCollection(it.categories)
        })
        dataViewModel.loadDomainCollection()
    }

    @SuppressLint("InflateParams")
    @OnClick(R.id.add_new_fab)
    fun onAddNewFabClicked() {
        val inflater = LayoutInflater.from(context)
        val inflatedView = inflater.inflate(R.layout.dialog, null)
        val addField = inflatedView.findViewById<EditText>(R.id.add_field)

        val dialog = AlertDialog.Builder(context!!)
            .setView(inflatedView)
            .setPositiveButton(R.string.add) { _, _ ->
                val category = Category(addField.text.toString().trim(), arrayListOf())
                categoryCollection?.categories?.add(category)
                Collections.sort(categoryCollection?.categories, CategoryComparator())
                dataViewModel.saveDomainCollection(categoryCollection)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false
        addField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                positiveButton.isEnabled = !s.toString().trim().isEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onCategoryClicked(category: Category) {
        val bundle = Bundle()
        bundle.putString(BundleConstants.CATEGORY, category.name)
        navController?.navigate(R.id.action_categoriesFragment_to_entryFragment, bundle)
    }

    override fun onDeleteCategory(category: Category) {
        AlertDialog.Builder(context!!)
            .setMessage(R.string.are_you_sure_you_want_to_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                categoryCollection?.categories?.remove(category)
                dataViewModel.saveDomainCollection(categoryCollection)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupDrawerClickListeners() {
        mainActivity?.getDrawerView()?.export_file_layout?.setOnClickListener {
            exportFile()
        }
        mainActivity?.onImportFileListener = this
        mainActivity?.getDrawerView()?.import_file_layout?.setOnClickListener {
            importFile()
        }
    }

    private fun importFile() {
        if (hasStoragePermissions(IMPORT_STORAGE_REQUEST_CODE)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // mime type
            }

            activity?.startActivityForResult(intent, READ_REQUEST_CODE)
        }

    }

    override fun onFileSelected(uri: Uri) {
        val jsonString: String = FileUtil.readTextFromUri(uri, context!!)
        try {
            val encryptedDataHolder: EncryptedDataHolder = gson.fromJson(jsonString, TypeUtil.genericType<EncryptedDataHolder>())
            val importedCategoryCollection: CategoryCollection = gson.fromJson(
                encryption.decryptString(
                    encryptedDataHolder.encryptedJson,
                    encryptedDataHolder.salt,
                    keyManager.getCombinedKey()!!
                ), TypeUtil.genericType<CategoryCollection>()
            )
            showMergeOrReplaceDialog(importedCategoryCollection)
        } catch (e: Exception) {
            when (e) {
                is GeneralSecurityException -> {
                    Toast.makeText(context, getString(R.string.file_encrypted_with_different_password), Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, getString(R.string.bad_file_type), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showMergeOrReplaceDialog(importedCategoryCollection: CategoryCollection) {
        AlertDialog.Builder(context!!)
            .setMessage(R.string.merge_or_replace)
            .setPositiveButton(R.string.merge) { _, _ ->
                merge(importedCategoryCollection)
            }
            .setNegativeButton(R.string.replace_all) { _, _ ->
                dataViewModel.saveDomainCollection(importedCategoryCollection)
            }
            .setNeutralButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun merge(importedCategoryCollection: CategoryCollection) {
        GlobalScope.launch {
            for (category in categoryCollection!!.categories) {
                if (importedCategoryCollection.getCategoryByName(category.name) == null) {
                    importedCategoryCollection.categories.add(category)
                } else {
                    val importedCategory = importedCategoryCollection.getCategoryByName(category.name)!!
                    for (entry in category.credentialsList) {
                        val importedEntry: Credentials? = importedCategoryCollection.getCredentialsByCategoryAndDescription(
                            category.name,
                            entry.description
                        )
                        if (importedEntry == null) {
                            importedCategory.credentialsList.add(entry)
                        } else if (importedEntry != entry) {
                            val countDownLatch = CountDownLatch(1)
                            appExecutors.mainThread().execute {
                                AlertDialog.Builder(context!!)
                                    .setTitle(entry.description)
                                    .setMessage(R.string.merge_conflict)
                                    .setPositiveButton(R.string.keep) { _, _ ->
                                        importedCategory.credentialsList.remove(importedEntry)
                                        importedCategory.credentialsList.add(entry)
                                        countDownLatch.countDown()
                                    }
                                    .setNegativeButton(R.string.replace) { _, _ ->
                                        countDownLatch.countDown()
                                    }
                                    .setCancelable(false)
                                    .create()
                                    .show()
                            }
                            countDownLatch.await()
                        }
                    }
                }
            }
            dataViewModel.saveDomainCollection(importedCategoryCollection)
        }
    }

    private fun exportFile() {
        if (hasStoragePermissions(EXPORT_STORAGE_REQUEST_CODE)) {
            if (FileUtil.isExternalStorageWritable()) {
                val encryptedDataHolder: EncryptedDataHolder =
                    encryption.encryptString(gson.toJson(categoryCollection), keyManager.getCombinedKey()!!)
                val fileContent = gson.toJson(encryptedDataHolder)
                FileUtil.writeStringToFile(getString(R.string.file_name) + ".json", fileContent)
            } else {
                Toast.makeText(context, getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasStoragePermissions(requestCode: Int): Boolean {
        val permission = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == EXPORT_STORAGE_REQUEST_CODE) {
                exportFile()
            } else if (requestCode == IMPORT_STORAGE_REQUEST_CODE) {
                importFile()
            }
        }
    }

    companion object {
        const val EXPORT_STORAGE_REQUEST_CODE = 345
        const val IMPORT_STORAGE_REQUEST_CODE = 346
    }
}