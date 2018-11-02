package com.ober.arctic

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import butterknife.ButterKnife
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

abstract class BaseFragment : Fragment() {

    protected var navController: NavController? = null

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val mainActivity: MainActivity?
        get() = if (activity != null) activity as MainActivity else null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mainActivity != null) {
            navController = Navigation.findNavController(mainActivity!!, R.id.nav_host_fragment)
        }
    }

    protected fun setAndBindContentView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup, @LayoutRes layoutResID: Int): View {
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
