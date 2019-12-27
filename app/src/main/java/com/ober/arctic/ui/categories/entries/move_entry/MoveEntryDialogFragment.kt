package com.ober.arctic.ui.categories.entries.move_entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.OnClick
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.Credentials
import com.ober.arctic.data.model.CredentialsComparator
import com.ober.arctic.ui.BaseDialogFragment
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.ui.ViewState
import com.ober.arcticpass.R
import com.ober.vmrlink.Success
import kotlinx.android.synthetic.main.fragment_file_list.*
import java.util.*

class MoveEntryDialogFragment : BaseDialogFragment(), MoveEntryRecyclerAdapter.CategoryClickedListener {

    private var adapter: MoveEntryRecyclerAdapter? = null

    private lateinit var dataViewModel: DataViewModel

    private var credentials: Credentials? = null

    private var currentCategoryName: String? = null

    private var categoryCollection: CategoryCollection? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = setAndBindContentView(inflater, container, R.layout.fragment_move_entry)
        dataViewModel = ViewModelProviders.of(mainActivity!!, viewModelFactory)[DataViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setViewState(ViewState.LOADING)
        setupRecyclerView()
        setupObservers()
    }

    @OnClick(R.id.cancel_button)
    fun onCancelClicked() {
        dismiss()
    }

    private fun setupRecyclerView() {
        adapter = MoveEntryRecyclerAdapter(this)
        files_recycler_view.adapter = adapter
        files_recycler_view.layoutManager = LinearLayoutManager(context)
    }

    private fun setupObservers() {
        dataViewModel.categoryCollectionLink.observe(this, Observer {
            when (it) {
                is Success -> {
                    categoryCollection = it.data
                    adapter?.categories = it.data?.categories
                    setViewState(ViewState.DATA)
                }
            }
        })
    }

    private fun setViewState(viewState: ViewState) {
        files_recycler_view.visibility = View.GONE
        loading_spinner.visibility = View.GONE
        when (viewState) {
            ViewState.LOADING -> {
                loading_spinner.visibility = View.VISIBLE
            }
            else -> {
                files_recycler_view.visibility = View.VISIBLE
            }
        }
    }

    override fun onCategoryClicked(categoryName: String?) {
        val category = categoryCollection?.categories?.find { it.name == categoryName }
        category?.credentialsList?.find { it.description == credentials?.description }?.let {
            Toast.makeText(context, getString(R.string.move_failed), Toast.LENGTH_SHORT).show()
            return
        }
        credentials?.let { credentials ->
            category?.credentialsList?.add(credentials)
            Collections.sort(category?.credentialsList, CredentialsComparator())
            categoryCollection?.categories?.find { it.name == currentCategoryName }
                ?.credentialsList?.remove(credentials)
            dataViewModel.categoryCollectionLink.save(categoryCollection)
        }
        dismiss()
    }

    companion object {
        fun newInstance(credentials: Credentials, currentCategoryName: String): MoveEntryDialogFragment {
            val fragment = MoveEntryDialogFragment()
            fragment.credentials = credentials
            fragment.currentCategoryName = currentCategoryName
            return fragment
        }
    }
}