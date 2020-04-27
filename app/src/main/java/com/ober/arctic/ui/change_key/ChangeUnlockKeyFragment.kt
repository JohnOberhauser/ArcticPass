package com.ober.arctic.ui.change_key

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import butterknife.OnClick
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.security.Encryption
import com.ober.arctic.util.security.FingerprintManager
import com.ober.arctic.util.security.KeyManager
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_change_unlock_key.*
import net.grandcentrix.tray.AppPreferences
import javax.inject.Inject

class ChangeUnlockKeyFragment : BaseFragment() {

    @Inject
    lateinit var encryption: Encryption

    @Inject
    lateinit var keyManager: KeyManager

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var fingerprintManager: FingerprintManager

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        return setAndBindContentView(inflater, container!!, R.layout.fragment_change_unlock_key)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEditTextListeners()
    }

    @OnClick(R.id.done_button)
    fun onDoneClicked() {
        done_button.isEnabled = false
        appExecutors.miscellaneousThread().execute {
            keyManager.getEncryptionKey()?.let { encryptionKey ->
                keyManager.unlockKey = unlock_password_field.text.toString().trim()
                keyManager.saveEncryptionKey(encryptionKey)
                if (fingerprintManager.isFingerprintEnabled()) {
                    fingerprintManager.enableFingerprint2(context!!)
                }
                appExecutors.mainThread().execute {
                    mainActivity?.onBackPressed()
                }
            }
        }
    }

    private fun setupEditTextListeners() {

        unlock_password_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                done_button.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        unlock_password_field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && done_button.isEnabled) {
                onDoneClicked()
            }
            false
        }
    }
}