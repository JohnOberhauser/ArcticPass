package com.ober.arctic.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.util.security.KeyManager
import com.ober.arcticpass.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashFragment : BaseFragment() {

    @Inject
    lateinit var keyManager: KeyManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        return setAndBindContentView(inflater, container!!, R.layout.fragment_splash)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch {
            if (keyManager.doesRecoveryKeyExist()) {
                navController?.navigate(R.id.action_splashFragment_to_unlockFragment)
            } else {
                navController?.navigate(R.id.action_splashFragment_to_initFragment)
            }
        }
    }
}