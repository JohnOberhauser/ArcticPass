package com.ober.arctic.ui.unlock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import butterknife.OnClick
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.security.KeyManager
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_unlock.*
import javax.inject.Inject

class UnlockFragment : BaseFragment() {

    @Inject
    lateinit var keyManager: KeyManager

    @Inject
    lateinit var appExecutors: AppExecutors

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
        password_field.requestFocus()
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

    @OnClick(R.id.unlock_button)
    fun onUnlockClicked() {
        unlock_button.isEnabled = false
        keyManager.setUnlockKey(password_field.text.toString().trim())
        appExecutors.miscellaneousThread().execute {
            if (keyManager.isUnlockKeyCorrect()) {
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