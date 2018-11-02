package com.ober.arctic.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.Credentials
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.BundleConstants
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_landing.*

class CategoryFragment : BaseFragment(), CredentialsRecyclerAdapter.CredentialsClickedListener {

    private lateinit var dataViewModel: DataViewModel

    private var credentialsAdapter: CredentialsRecyclerAdapter? = null

    private var categoryCollection: CategoryCollection? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        dataViewModel = ViewModelProviders.of(this, viewModelFactory)[DataViewModel::class.java]
        return setAndBindContentView(inflater, container!!, R.layout.fragment_category)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObserver()
    }

    private fun setupRecyclerView() {
        credentialsAdapter = CredentialsRecyclerAdapter(this)
        domain_recycler_view.adapter = credentialsAdapter
        domain_recycler_view.layoutManager = LinearLayoutManager(context)
    }

    private fun setupObserver() {
        dataViewModel.domainCollectionLiveData.observe(this, Observer {
            progress_bar.visibility = View.GONE
            credentialsAdapter?.credentials = it.categories.find { category -> category.name == arguments?.getString(BundleConstants.CATEGORY) }!!.credentialsList
            categoryCollection = CategoryCollection(it.categories)
        })
        dataViewModel.loadDomainCollection()
    }

    override fun onCredentialClicked(credentials: Credentials) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeleteCredential(credentials: Credentials) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}