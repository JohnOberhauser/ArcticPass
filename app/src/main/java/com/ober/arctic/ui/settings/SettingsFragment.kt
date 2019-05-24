package com.ober.arctic.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ober.arctic.App
import com.ober.arctic.ui.BaseFragment
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_settings.*
import net.grandcentrix.tray.AppPreferences
import javax.inject.Inject

class SettingsFragment : BaseFragment() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.appComponent!!.inject(this)
        return setAndBindContentView(inflater, container!!, R.layout.fragment_settings)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRadioListeners()
        setupCheckListener()
        when (appPreferences.getInt(TIMEOUT, T_5_MINUTES)) {
            T_30_SECONDS -> _30_seconds_radio_button.isChecked = true
            T_1_MINUTE -> _1_minute_radio_button.isChecked = true
            T_2_MINUTES -> _2_minutes_radio_button.isChecked = true
            T_3_MINUTES -> _3_minutes_radio_button.isChecked = true
            T_5_MINUTES -> _5_minutes_radio_button.isChecked = true
            T_10_MINUTES -> _10_minutes_radio_button.isChecked = true
            NO_TIMEOUT -> no_timeout_radio_button.isChecked = true
        }

        if (appPreferences.getBoolean(SCREEN_LOCK, true)) {
            lock_when_screen_off_check_box.isChecked = true
        }
    }

    private fun setupRadioListeners() {
        _30_seconds_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, T_30_SECONDS)
            }
        }
        _1_minute_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, T_1_MINUTE)
            }
        }
        _2_minutes_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, T_2_MINUTES)
            }
        }
        _3_minutes_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, T_3_MINUTES)
            }
        }
        _5_minutes_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, T_5_MINUTES)
            }
        }
        _10_minutes_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, T_10_MINUTES)
            }
        }
        no_timeout_radio_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.put(TIMEOUT, NO_TIMEOUT)
            }
        }
    }

    private fun setupCheckListener() {
        lock_when_screen_off_check_box.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.put(SCREEN_LOCK, isChecked)
        }
    }

    companion object {
        const val T_30_SECONDS = 30 * 1000
        const val T_1_MINUTE = 60 * 1000
        const val T_2_MINUTES = 2 * 60 * 1000
        const val T_3_MINUTES = 3 * 60 * 1000
        const val T_5_MINUTES = 5 * 60 * 1000
        const val T_10_MINUTES = 10 * 60 * 1000
        const val NO_TIMEOUT = -1

        const val TIMEOUT = "timeout"
        const val SCREEN_LOCK = "screen_lock"
    }
}