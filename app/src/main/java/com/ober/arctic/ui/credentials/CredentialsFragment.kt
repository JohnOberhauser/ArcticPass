package com.ober.arctic.ui.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.data.model.Category
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.Credentials
import com.ober.arctic.ui.DataViewModel
import com.ober.arctic.util.BundleConstants
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_credentials.*

class CredentialsFragment : BaseFragment() {

    private lateinit var dataViewModel: DataViewModel

    private var categoryCollection: CategoryCollection? = null

    private var category: Category? = null

    private var credentials: Credentials = Credentials()

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

            setText()
        })
        dataViewModel.loadDomainCollection()
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
        mainActivity?.enableSaveButton(View.OnClickListener {
            onSaveClicked()
        })

        description_text_view.visibility = View.GONE
        website_text_view.visibility = View.GONE
        username_text_view.visibility = View.GONE
        password_text_view.visibility = View.GONE
        notes_text_view.visibility = View.GONE

        description_field.visibility = View.VISIBLE
        website_field.visibility = View.VISIBLE
        username_field.visibility = View.VISIBLE
        password_field.visibility = View.VISIBLE
        notes_field.visibility = View.VISIBLE
    }

    private fun showSavedView() {
        mainActivity?.enableEditButton(View.OnClickListener {
            onEditClicked()
        })

        description_text_view.visibility = View.VISIBLE
        website_text_view.visibility = View.VISIBLE
        username_text_view.visibility = View.VISIBLE
        password_text_view.visibility = View.VISIBLE
        notes_text_view.visibility = View.VISIBLE

        description_field.visibility = View.GONE
        website_field.visibility = View.GONE
        username_field.visibility = View.GONE
        password_field.visibility = View.GONE
        notes_field.visibility = View.GONE
    }

    private fun onSaveClicked() {
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

                dataViewModel.saveDomainCollection(categoryCollection)

                showSavedView()
            }
        } else {
            Toast.makeText(context, R.string.description_already_exists, Toast.LENGTH_LONG).show()
        }
    }

    private fun onEditClicked() {
        showEditView()
    }
}