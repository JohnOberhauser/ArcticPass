package com.ober.arctic.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_landing.*

class LandingFragment : BaseFragment() {

    private lateinit var landingViewModel: LandingViewModel

    private var domainAdapter: DomainRecyclerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        landingViewModel = ViewModelProviders.of(this, viewModelFactory)[LandingViewModel::class.java]
        return setAndBindContentView(inflater, container!!, R.layout.fragment_landing)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObserver()
    }

    private fun setupRecyclerView() {
        domainAdapter = DomainRecyclerAdapter()
        domain_recycler_view.adapter = domainAdapter
        domain_recycler_view.layoutManager = LinearLayoutManager(context)
    }

    private fun setupObserver() {
        landingViewModel.domainCollectionLiveData.observe(this, Observer {
            progress_bar.visibility = View.GONE
            domainAdapter?.domains = it.domains
        })
        landingViewModel.loadDomainCollection()
    }
}