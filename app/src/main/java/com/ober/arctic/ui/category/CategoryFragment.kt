package com.ober.arctic.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arcticpass.R

class CategoryFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        return setAndBindContentView(inflater, container!!, R.layout.fragment_category)
    }
}