package com.ober.arctic.ui.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.ui.DataViewModel
import com.ober.arcticpass.R

class CredentialsFragment : BaseFragment() {

    private lateinit var dataViewModel: DataViewModel

    private var categoryCollection: CategoryCollection? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        dataViewModel = ViewModelProviders.of(this, viewModelFactory)[DataViewModel::class.java]
        return setAndBindContentView(inflater, container!!, R.layout.fragment_credentials)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
    }

    private fun setupObserver() {
        dataViewModel.domainCollectionLiveData.observe(this, Observer {
            categoryCollection = CategoryCollection(it.categories)
        })
        dataViewModel.loadDomainCollection()
    }
}