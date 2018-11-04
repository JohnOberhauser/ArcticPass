package com.ober.arctic

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation.findNavController
import com.ober.arctic.ui.category.CategoryFragment
import com.ober.arctic.ui.landing.LandingFragment
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.view.*

class MainActivity : AppCompatActivity() {

    private var drawerIcon: DrawerArrowDrawable? = null
    private var onBackPressedListener: OnBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupThemeSwitch()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        drawerIcon = DrawerArrowDrawable(this)
        drawerIcon?.color = ContextCompat.getColor(this, android.R.color.white)
        toolbar?.navigationIcon = drawerIcon
        enableDrawer()
        findNavController(this, R.id.nav_host_fragment).addOnNavigatedListener { _, destination ->
            if (destination.label == LandingFragment::class.java.simpleName) {
                enableDrawer()
            } else if (destination.label == CategoryFragment::class.java.simpleName) {
                enableBackButton()
                disableEditButton()
                disableSaveButton()
                onBackPressedListener = null
            }
            hideKeyboard()
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

    private fun setupThemeSwitch() {
//        nav_view.getHeaderView(0).theme_switch.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                theme.applyStyle(R.style.AppThemeDark, true)
//                recreate()
//            } else {
//                theme.applyStyle(R.style.AppTheme, true)
//                recreate()
//            }
//        }
    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()
}

interface OnBackPressedListener {
    fun onBackPressed(): Boolean
}