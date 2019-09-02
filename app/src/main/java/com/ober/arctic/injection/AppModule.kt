package com.ober.arctic.injection

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ober.arctic.App
import com.ober.arctic.data.database.MainDatabase
import com.ober.arctic.ui.BaseDialogFragment
import com.ober.arctic.ui.BaseFragment
import com.ober.arctic.ui.MainActivity
import com.ober.arctic.ui.categories.CategoriesFragment
import com.ober.arctic.ui.change_key.ChangeEncryptionKeyFragment
import com.ober.arctic.ui.change_key.ChangeUnlockKeyFragment
import com.ober.arctic.ui.categories.entries.credentials.CredentialsFragment
import com.ober.arctic.ui.init.InitFragment
import com.ober.arctic.ui.settings.SettingsFragment
import com.ober.arctic.ui.splash.SplashFragment
import com.ober.arctic.ui.unlock.UnlockFragment
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.AppExecutorsImpl
import com.ober.arctic.util.DriveServiceHolder
import com.ober.arctic.util.DriveServiceHolderImpl
import com.ober.arctic.util.security.*
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
        return EncryptionImpl()
    }

    @Provides
    @Singleton
    internal fun provideFingerprintManager(appPreferences: AppPreferences, keyManager: KeyManager): FingerprintManager {
        return FingerprintManagerImpl(appPreferences, keyManager)
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
    internal fun provideAppExecutors(): AppExecutors {
        return AppExecutorsImpl()
    }

    @Provides
    @Singleton
    internal fun provideDriveServiceHolder(): DriveServiceHolder {
        return DriveServiceHolderImpl()
    }

    @Singleton
    @Component(modules = [AppModule::class, ViewModelModule::class])
    interface AppComponent {

        // Inject functions ============================
        fun inject(app: App)

        fun inject(mainActivity: MainActivity)

        fun inject(baseFragment: BaseFragment)

        fun inject(baseDialogFragment: BaseDialogFragment)

        fun inject(initFragment: InitFragment)
        fun inject(splashFragment: SplashFragment)
        fun inject(unlockFragment: UnlockFragment)
        fun inject(credentialsFragment: CredentialsFragment)
        fun inject(categoriesFragment: CategoriesFragment)
        fun inject(changeEncryptionKeyFragment: ChangeEncryptionKeyFragment)
        fun inject(changeUnlockKeyFragment: ChangeUnlockKeyFragment)
        fun inject(settingsFragment: SettingsFragment)

        // Tools  ======================================
        fun gson(): Gson

        fun encryption(): Encryption

        fun keyManager(): KeyManager

        fun viewModelFactory(): ViewModelFactory

        fun mainDatabase(): MainDatabase

        fun appExecutors(): AppExecutors
    }
}
