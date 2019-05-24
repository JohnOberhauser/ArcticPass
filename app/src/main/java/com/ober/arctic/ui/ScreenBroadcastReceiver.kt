package com.ober.arctic.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenBroadcastReceiver : BroadcastReceiver() {

    var needToLogout = false

    override fun onReceive(context: Context?, intent: Intent?) {
        needToLogout = true
    }

}