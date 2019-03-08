package com.ober.arctic

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation.findNavController
import com.ober.arctic.ui.entries.EntriesFragment
import com.ober.arctic.ui.categories.CategoriesFragment
import com.ober.arctic.ui.credentials.CredentialsFragment
import com.ober.arctic.ui.unlock.UnlockFragment
import com.ober.arctic.util.AppExecutors
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.view.*
import net.grandcentrix.tray.AppPreferences
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var appExecutors: AppExecutors

    private var drawerIcon: DrawerArrowDrawable? = null
    private var onBackPressedListener: OnBackPressedListener? = null
    var onImportFileListener: OnImportFileListener? = null
    var onSyncWithGoogleListener: OnSyncWithGoogleListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent?.inject(this)
        setTheme()
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupDrawerClickListeners()
        setupNavControllerListener()
    }

    private fun setTheme() {
        when (appPreferences.getString(THEME, LIGHT)) {
            LIGHT -> setTheme(R.style.AppTheme)
            DARK -> setTheme(R.style.AppThemeDark)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        drawerIcon = DrawerArrowDrawable(this)
        drawerIcon?.color = ContextCompat.getColor(this, android.R.color.white)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        enableDrawer()
    }

    private fun setupNavControllerListener() {
        findNavController(this, R.id.nav_host_fragment).addOnDestinationChangedListener { _, destination, _ ->
            appExecutors.mainThread().execute {
                when (destination.label) {
                    CategoriesFragment::class.java.simpleName -> {
                        toolbar?.navigationIcon = drawerIcon
                        enableDrawer()
                        toolbar_title.text = getString(R.string.categories)
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                    EntriesFragment::class.java.simpleName -> {
                        enableBackButton()
                        disableEditButton()
                        disableSaveButton()
                        onBackPressedListener = null
                        toolbar_title.text = getString(R.string.entries)
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                    CredentialsFragment::class.java.simpleName -> toolbar_title.text = getString(R.string.credentials)
                    UnlockFragment::class.java.simpleName -> {
                        toolbar_title.text = ""
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                }
                hideKeyboard()
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    private fun enableBackButton() {
        ObjectAnimator.ofFloat(drawerIcon!!, "progress", 1f).start()
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun enableDrawer() {
        ObjectAnimator.ofFloat(drawerIcon!!, "progress", 0f).start()
        toolbar?.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers()
        } else if (onBackPressedListener == null || !onBackPressedListener!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    fun enableSaveButton(onSaveClickedListener: View.OnClickListener?, onBackPressedListener: OnBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener
        disableEditButton()
        save_button.visibility = View.VISIBLE
        save_button.setOnClickListener(onSaveClickedListener)
    }

    private fun disableSaveButton() {
        save_button.visibility = View.GONE
        save_button.setOnClickListener(null)
    }

    fun enableEditButton(onEditClickedListener: View.OnClickListener?) {
        this.onBackPressedListener = null
        disableSaveButton()
        edit_button.visibility = View.VISIBLE
        edit_button.setOnClickListener(onEditClickedListener)
    }

    private fun disableEditButton() {
        edit_button.visibility = View.GONE
        edit_button.setOnClickListener(null)
    }

    private fun setupDrawerClickListeners() {
        val switch = nav_view.getHeaderView(0).theme_switch
        when (appPreferences.getString(THEME, LIGHT)) {
            LIGHT -> switch.isChecked = false
            DARK -> switch.isChecked = true
        }
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(THEME, DARK)
            } else {
                appPreferences.put(THEME, LIGHT)
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun getDrawerView(): View {
        return nav_view.getHeaderView(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                onImportFileListener?.onFileSelected(uri)
            }
        } else if (resultCode == Activity.RESULT_OK) {
            onSyncWithGoogleListener?.onSyncComplete()
            println(data?.extras?.get("Error"))
        }

    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()

    companion object {
        const val THEME = "theme"
        const val DARK = "dark"
        const val LIGHT = "light"
        const val READ_REQUEST_CODE = 32
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 33
    }
}

interface OnBackPressedListener {
    fun onBackPressed(): Boolean
}

interface OnImportFileListener {
    fun onFileSelected(uri: Uri)
}

interface OnSyncWithGoogleListener {
    fun onSyncComplete()
}