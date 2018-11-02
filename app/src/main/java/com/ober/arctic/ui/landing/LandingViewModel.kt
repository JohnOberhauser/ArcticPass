package com.ober.arctic.ui.landing

import androidx.lifecycle.ViewModel
import com.ober.arctic.repository.DataRepository
import javax.inject.Inject

class LandingViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val domainCollectionLiveData = dataRepository.getDomainCollectionLiveData()

    fun loadDomainCollection() {
        dataRepository.loadDomainCollection(true)
    }
}