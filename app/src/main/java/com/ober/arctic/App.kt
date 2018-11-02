package com.ober.arctic

import androidx.multidex.MultiDexApplication
import com.ober.arctic.injection.AppModule
import com.ober.arctic.injection.DaggerAppModule_AppComponent

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        app = this
        appComponent = DaggerAppModule_AppComponent.builder()
            .appModule(AppModule(this))
            .build()

        appComponent!!.inject(this)
    }

    companion object {

        var app: App? = null
            private set
        var appComponent: AppModule.AppComponent? = null
            private set
    }

}
