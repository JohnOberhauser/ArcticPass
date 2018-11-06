package com.ober.arctic.ui.categories

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.OnClick
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.data.model.Category
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.CategoryComparator
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.BundleConstants
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_landing.*
import java.util.*

class CategoriesFragment : BaseFragment(), CategoryRecyclerAdapter.CategoryClickedListener {

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
}