package com.ober.arctic.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import butterknife.ButterKnife
import com.ober.arctic.App
import javax.inject.Inject

abstract class BaseDialogFragment: DialogFragment() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val mainActivity: MainActivity?
        get() = if (activity != null) activity as MainActivity else null

    protected fun setAndBindContentView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?, @LayoutRes layoutResID: Int): View {
        val inflatedView = inflater.inflate(layoutResID, container, false)
        App.appComponent!!.inject(this)
        ButterKnife.bind(this, inflatedView)
        return inflatedView
    }

    protected fun hideKeyboard() {
        val inputMethodManager = mainActivity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}