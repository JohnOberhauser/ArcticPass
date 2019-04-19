package com.ober.arctic.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.OnClick
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.Credentials
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.BundleConstants
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_category.*

class EntriesFragment : BaseFragment(), CredentialsRecyclerAdapter.CredentialsClickedListener {

    private lateinit var dataViewModel: DataViewModel

    private var credentialsAdapter: CredentialsRecyclerAdapter? = null

    private var categoryCollection: CategoryCollection? = null

    private var categoryName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        dataViewModel = ViewModelProviders.of(this, viewModelFactory)[DataViewModel::class.java]
        categoryName = arguments?.getString(BundleConstants.CATEGORY)
        return setAndBindContentView(inflater, container!!, R.layout.fragment_category)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObserver()
    }

    private fun setupRecyclerView() {
        credentialsAdapter = CredentialsRecyclerAdapter(this)
        credentials_recycler_view.adapter = credentialsAdapter
        credentials_recycler_view.layoutManager = LinearLayoutManager(context)
    }

    private fun setupObserver() {
        dataViewModel.categoryCollectionLiveData.observe(this, Observer {
            progress_bar.visibility = View.GONE
            categoryCollection = CategoryCollection(it.categories)
            credentialsAdapter?.credentials =
                    categoryCollection?.getCategoryByName(categoryName)?.credentialsList!!

        })
        dataViewModel.loadDomainCollection()
    }

    override fun onCredentialClicked(credentials: Credentials) {
        val bundle = Bundle()
        bundle.putString(BundleConstants.CATEGORY, categoryName)
        bundle.putString(BundleConstants.CREDENTIALS_DESCRIPTION, credentials.description)
        navController?.navigate(R.id.action_entryFragment_to_credentialsFragment, bundle)
    }

    override fun onDeleteCredential(credentials: Credentials) {
        AlertDialog.Builder(context!!)
            .setMessage(R.string.are_you_sure_you_want_to_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                categoryCollection?.getCategoryByName(categoryName)?.credentialsList?.remove(
                    credentials
                )
                dataViewModel.saveCategoryCollection(categoryCollection)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @OnClick(R.id.add_new_fab)
    fun onAddNewFabClicked() {
        val bundle = Bundle()
        bundle.putBoolean(BundleConstants.NEW_CREDENTIALS, true)
        bundle.putString(BundleConstants.CATEGORY, categoryName)
        navController?.navigate(R.id.action_entryFragment_to_credentialsFragment, bundle)
    }
}