package com.ober.arctic.ui.unlock

import androidx.lifecycle.ViewModel
import com.ober.arctic.repository.DataRepository
import javax.inject.Inject

class UnlockViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val domainCollectionLiveData = dataRepository.getCategoryCollectionLiveData()

    fun loadDomainCollection() {
        dataRepository.loadCategoryCollection(false)
    }
}