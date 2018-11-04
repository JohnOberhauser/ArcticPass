package com.ober.arctic

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation.findNavController
import com.ober.arctic.ui.category.CategoryFragment
import com.ober.arctic.ui.landing.LandingFragment
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var drawerIcon: DrawerArrowDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        setupToolbar()
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
            }
        }
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
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()
}
