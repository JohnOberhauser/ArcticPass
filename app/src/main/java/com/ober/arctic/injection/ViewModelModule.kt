package com.ober.arctic.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ober.arctic.ui.init.InitViewModel
import com.ober.arctic.ui.landing.LandingViewModel
import com.ober.arctic.ui.splash.SplashViewModel
import com.ober.arctic.ui.unlock.UnlockViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(InitViewModel::class)
    internal abstract fun bindInitViewModel(viewModel: InitViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UnlockViewModel::class)
    internal abstract fun bindUnlockViewModel(viewModel: UnlockViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LandingViewModel::class)
    internal abstract fun bindLandingViewModel(viewModel: LandingViewModel): ViewModel

}

@Singleton
class ViewModelFactory @Inject constructor(private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        viewModels[modelClass]?.get() as T
}

@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

