package com.ober.arctic.ui.unlock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.CancellationSignal
import butterknife.OnClick
import com.mtramin.rxfingerprint.RxFingerprint
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.security.*
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_settings.*
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

    private val cancellationSignal = CancellationSignal()

    private var fingerprintNeedsToResave = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        if (keyManager.isUnlockKeyCorrect()) {
            navController?.navigate(R.id.action_unlockFragment_to_categoriesFragment)
        }
        return setAndBindContentView(inflater, container!!, R.layout.fragment_unlock)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEditTextListeners()
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
            && !appPreferences.getString(FingerprintManagerImpl.ENCRYPTED_DATA, null).isNullOrBlank()
            && appPreferences.getBoolean(FingerprintManagerImpl.FINGERPRINT_ENABLED, false)
        ) {
            fingerprintManager.authenticateAndDecrypt(context!!, cancellationSignal, object : FingerprintDecryptCallback {
                override fun onSuccess(data: String) {
                    keyManager.unlockKey = data
                    if (keyManager.isUnlockKeyCorrect()) {
                        navController?.navigate(R.id.action_unlockFragment_to_categoriesFragment)
                    } else {
                        fingerprintNeedsToResave = true
                        showPasswordLayout()
                    }
                }

                override fun onInvalid() {
                    fingerprintNeedsToResave = true
                    showPasswordLayout()
                }
            })
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

    @OnClick(R.id.enter_password_button)
    fun onEnterPasswordClicked() {
        showPasswordLayout()
    }

    @OnClick(R.id.unlock_button)
    fun onUnlockClicked() {
        unlock_button.isEnabled = false
        keyManager.unlockKey = password_field.text.toString().trim()
        appExecutors.miscellaneousThread().execute {
            if (keyManager.isUnlockKeyCorrect()) {
                if (fingerprintNeedsToResave) {
                    keyManager.unlockKey?.let {
                        GlobalScope.launch {
                            fingerprintManager.authenticateAndEncrypt(context!!, it)
                        }
                    }
                }
                appExecutors.mainThread().execute {
                    navController?.navigate(R.id.action_unlockFragment_to_categoriesFragment)
                }
            } else {
                appExecutors.mainThread().execute {
                    val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
                    password_field.startAnimation(shake)
                    password_field.setText("")
                }
            }
        }
    }
}