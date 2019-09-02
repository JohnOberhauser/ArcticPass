package com.ober.arctic.ui.unlock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.core.os.CancellationSignal
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import com.mattprecious.swirl.SwirlView
import com.mtramin.rxfingerprint.RxFingerprint
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.security.*
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_unlock.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.grandcentrix.tray.AppPreferences
import javax.inject.Inject

class UnlockFragment : BaseFragment() {

    @Inject
    lateinit var keyManager: KeyManager

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var fingerprintManager: FingerprintManager

    private lateinit var dataViewModel: DataViewModel

    private val cancellationSignal = CancellationSignal()

    private var fingerprintNeedsToReSave = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        dataViewModel = ViewModelProviders.of(mainActivity!!, viewModelFactory)[DataViewModel::class.java]
        if (keyManager.isUnlockKeyCorrect()) {
            navController?.navigate(R.id.action_unlockFragment_to_categoriesFragment)
        }
        return setAndBindContentView(inflater, container!!, R.layout.fragment_unlock)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setupEditTextListeners()
    }

    override fun onResume() {
        super.onResume()
        setupFingerprintUnlock()
    }

    private fun setupEditTextListeners() {

        password_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                unlock_button.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        password_field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && unlock_button.isEnabled) {
                onUnlockClicked()
            }
            false
        }
    }

    private fun setupFingerprintUnlock() {
        if (RxFingerprint.isAvailable(context!!)
            && fingerprintManager.isFingerprintEnabled()
        ) {
            fingerprintManager.authenticateAndSetUnlockKey(
                context!!,
                cancellationSignal,
                object : FingerprintAuthenticatedCallback {
                    override fun onSuccess() {
                        fingerprint_swirl.setState(SwirlView.State.OFF, true)
                        attemptUnlock()
                    }

                    override fun onInvalid() {
                        fingerprintNeedsToReSave = true
                        showPasswordLayout()
                    }
                })

            fingerprint_swirl.setState(SwirlView.State.ON, true)
        } else {
            showPasswordLayout()
        }
    }

    private fun showPasswordLayout() {
        cancellationSignal.cancel()
        fingerprint_layout.visibility = View.GONE
        password_layout.visibility = View.VISIBLE
        password_field.requestFocus()
        handler.post {
            showKeyboard()
        }
    }

    private fun isPasswordLayoutShowing(): Boolean {
        return password_layout.visibility == View.VISIBLE
    }

    @OnClick(R.id.enter_password_button)
    fun onEnterPasswordClicked() {
        showPasswordLayout()
    }

    @OnClick(R.id.unlock_button)
    fun onUnlockClicked() {
        unlock_button.isEnabled = false
        keyManager.unlockKey = password_field.text.toString().trim()
        attemptUnlock()
    }

    private fun attemptUnlock() {
        GlobalScope.launch {
            val unlockKeyCorrect = keyManager.isUnlockKeyCorrect()
            appExecutors.mainThread().execute {
                if (unlockKeyCorrect) {
                    hideKeyboard()
                    dataViewModel.categoryCollectionLink.update()
                    if (fingerprintNeedsToReSave) {
                        fingerprintManager.enableFingerprint(context!!)
                    }
                } else {
                    if (isPasswordLayoutShowing()) {
                        val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
                        password_field.startAnimation(shake)
                        password_field.setText("")
                    } else {
                        fingerprintNeedsToReSave = true
                        showPasswordLayout()
                    }
                }
            }
        }
    }

    private fun setupObserver() {
        dataViewModel.categoryCollectionLink.value.observe(this, Observer {
            navController?.navigate(R.id.action_unlockFragment_to_categoriesFragment)
        })
    }
}