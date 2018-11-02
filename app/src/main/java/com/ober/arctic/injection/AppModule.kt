package com.ober.arctic.injection

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ober.arctic.App
import com.ober.arctic.BaseFragment
import com.ober.arctic.MainActivity
import com.ober.arctic.data.cache.LiveDataHolder
import com.ober.arctic.data.cache.LiveDataHolderImpl
import com.ober.arctic.data.database.MainDatabase
import com.ober.arctic.ui.init.InitFragment
import com.ober.arctic.ui.splash.SplashFragment
import com.ober.arctic.ui.unlock.UnlockFragment
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.AppExecutorsImpl
import com.ober.arctic.util.security.Encryption
import com.ober.arctic.util.security.EncryptionImpl
import com.ober.arctic.util.security.KeyManager
import com.ober.arctic.util.security.KeyManagerImpl
import dagger.Component
import dagger.Module
import dagger.Provides
import net.grandcentrix.tray.AppPreferences
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {

    @Provides
    internal fun provideApp(): App {
        return app
    }

    @Provides
    @Singleton
    internal fun provideEncryption(): Encryption {
        return EncryptionImpl(app)
    }

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    @Singleton
    internal fun provideAppPreferences(): AppPreferences {
        return AppPreferences(app)
    }

    @Provides
    @Singleton
    internal fun provideMainDatabase(): MainDatabase {
        return Room.databaseBuilder(app, MainDatabase::class.java, "main_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    internal fun provideKeyManager(encryption: Encryption, appPreferences: AppPreferences): KeyManager {
        return KeyManagerImpl(appPreferences, encryption)
    }

    @Provides
    @Singleton
    internal fun provideLiveDataHolder(): LiveDataHolder {
        return LiveDataHolderImpl()
    }

    @Provides
    @Singleton
    internal fun provideAppExecutors(): AppExecutors {
        return AppExecutorsImpl()
    }

    @Singleton
    @Component(modules = [AppModule::class, ViewModelModule::class])
    interface AppComponent {

        // Inject functions ============================
        fun inject(app: App)

        fun inject(mainActivity: MainActivity)

        fun inject(baseFragment: BaseFragment)

        fun inject(initFragment: InitFragment)
        fun inject(splashFragment: SplashFragment)
        fun inject(unlockFragment: UnlockFragment)

        // Tools  ======================================
        fun gson(): Gson

        fun encryption(): Encryption

        fun keyManager(): KeyManager

        fun viewModelFactory(): ViewModelFactory

        fun mainDatabase(): MainDatabase

        fun liveDataHolder(): LiveDataHolder

        fun appExecutors(): AppExecutors
    }
}
