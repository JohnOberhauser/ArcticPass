package com.ober.arctic.ui.credentials

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.data.model.Category
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.Credentials
import com.ober.arctic.data.model.CredentialsComparator
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.BundleConstants
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_credentials.*
import java.util.*
import androidx.core.content.ContextCompat.getSystemService
import butterknife.OnClick
import com.ober.arctic.ui.OnBackPressedListener
import com.ober.arctic.util.security.Encryption
import javax.inject.Inject


class CredentialsFragment : BaseFragment(), OnBackPressedListener {

    @Inject
    lateinit var encryption: Encryption

    private lateinit var dataViewModel: DataViewModel

    private var categoryCollection: CategoryCollection? = null

    private var category: Category? = null

    private var credentials: Credentials = Credentials()

    private var inEditMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        dataViewModel = ViewModelProviders.of(this, viewModelFactory)[DataViewModel::class.java]
        return setAndBindContentView(inflater, container!!, R.layout.fragment_credentials)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null && arguments!!.getBoolean(BundleConstants.NEW_CREDENTIALS, false)) {
            showEditView()
        } else {
            showSavedView()
        }
        setupSeekBar()
        setupObserver()
    }

    private fun setupObserver() {
        dataViewModel.domainCollectionLiveData.observe(this, Observer {
            categoryCollection = CategoryCollection(it.categories)

            category = categoryCollection?.getCategoryByName(arguments?.getString(BundleConstants.CATEGORY))

            val credentials = categoryCollection?.getCredentialsByCategoryAndDescription(
                category?.name,
                arguments?.getString(BundleConstants.CREDENTIALS_DESCRIPTION)
            )
            if (credentials != null) {
                this.credentials = credentials
            }

            if (inEditMode) {
                showEditView()
            } else {
                showSavedView()
            }
            setText()
        })
        dataViewModel.loadDomainCollection()
    }

    private fun setupSeekBar() {
        password_length_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val text = "${progress + MINIMUM_PASSWORD_LENGTH}"
                password_length_text_view.text = text
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        password_length_seek_bar.progress = MINIMUM_PASSWORD_LENGTH
        val text = "${password_length_seek_bar.progress + MINIMUM_PASSWORD_LENGTH}"
        password_length_text_view.text = text
    }

    @OnClick(R.id.generate_button)
    fun onGenerateClicked() {
        password_field.setText(encryption.generateRandomKey(password_length_seek_bar.progress + MINIMUM_PASSWORD_LENGTH))
    }

    private fun setText() {
        description_text_view.text = credentials.description
        description_field.setText(credentials.description)
        website_text_view.text = credentials.website
        website_field.setText(credentials.website)
        username_text_view.text = credentials.username
        username_field.setText(credentials.username)
        password_text_view.text = credentials.password
        password_field.setText(credentials.password)
        notes_text_view.text = credentials.notes
        notes_field.setText(credentials.notes)
    }

    private fun showEditView() {
        inEditMode = true
        mainActivity?.enableSaveButton(View.OnClickListener {
            onSaveClicked()
        }, this)

        description_text_view.visibility = View.GONE
        website_text_view.visibility = View.GONE
        username_text_view.visibility = View.GONE
        password_text_view.visibility = View.GONE
        notes_text_view.visibility = View.GONE

        description_field_layout.visibility = View.VISIBLE
        website_field_layout.visibility = View.VISIBLE
        username_field_layout.visibility = View.VISIBLE
        password_field_layout.visibility = View.VISIBLE
        notes_field.visibility = View.VISIBLE
        password_generator_layout.visibility = View.VISIBLE

        username_layout.setOnLongClickListener(null)
        password_layout.setOnLongClickListener(null)
    }

    private fun showSavedView() {
        inEditMode = false
        mainActivity?.enableEditButton(View.OnClickListener {
            onEditClicked()
        })

        description_text_view.visibility = View.VISIBLE
        website_text_view.visibility = View.VISIBLE
        username_text_view.visibility = View.VISIBLE
        password_text_view.visibility = View.VISIBLE
        notes_text_view.visibility = View.VISIBLE

        description_field_layout.visibility = View.GONE
        website_field_layout.visibility = View.GONE
        username_field_layout.visibility = View.GONE
        password_field_layout.visibility = View.GONE
        notes_field.visibility = View.GONE
        password_generator_layout.visibility = View.GONE

        username_layout.setOnLongClickListener(this::onUserNameLongClick)
        password_layout.setOnLongClickListener(this::onPasswordLongClick)
    }

    private fun onUserNameLongClick(view: View): Boolean {
        val clipboard = getSystemService(context!!, ClipboardManager::class.java)
        clipboard?.primaryClip = ClipData.newPlainText("", username_text_view.text)
        Toast.makeText(context, R.string.username_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        return true
    }

    private fun onPasswordLongClick(view: View): Boolean {
        val clipboard = getSystemService(context!!, ClipboardManager::class.java)
        clipboard?.primaryClip = ClipData.newPlainText("", password_text_view.text)
        Toast.makeText(context, R.string.password_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        return true
    }

    private fun onSaveClicked() {
        hideKeyboard()
        val enteredDescriptionText = description_field.text.toString().trim()
        val credentialsWithWantedDescription =
            categoryCollection?.getCredentialsByCategoryAndDescription(category?.name, enteredDescriptionText)
        if (credentialsWithWantedDescription == null || credentialsWithWantedDescription == credentials) {
            if (category != null) {
                val newCredentials = Credentials()
                newCredentials.description = enteredDescriptionText
                newCredentials.website = website_field.text.toString().trim()
                newCredentials.username = username_field.text.toString().trim()
                newCredentials.password = password_field.text.toString().trim()
                newCredentials.notes = notes_field.text.toString().trim()

                if (newCredentials.description.isNullOrBlank()) {
                    Toast.makeText(context, R.string.description_must_not_be_blank, Toast.LENGTH_LONG).show()
                    return
                }

                category!!.credentialsList.remove(credentials)
                credentials = newCredentials
                category!!.credentialsList.add(credentials)

                Collections.sort(category!!.credentialsList, CredentialsComparator())

                inEditMode = false

                dataViewModel.saveDomainCollection(categoryCollection)
            }
        } else {
            Toast.makeText(context, R.string.description_already_exists, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed(): Boolean {
        if (arguments?.getString(BundleConstants.CREDENTIALS_DESCRIPTION) != null) {
            showSavedView()
            return true
        }
        return false
    }

    private fun onEditClicked() {
        showEditView()
    }

    companion object {
        const val MINIMUM_PASSWORD_LENGTH = 8
    }
}