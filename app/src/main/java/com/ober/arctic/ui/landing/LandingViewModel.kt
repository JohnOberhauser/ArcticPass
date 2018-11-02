package com.ober.arctic.ui.landing

import androidx.lifecycle.ViewModel
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import javax.inject.Inject

class LandingViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val domainCollectionLiveData = dataRepository.getCategoryCollectionLiveData()

    fun loadDomainCollection() {
        dataRepository.loadCategoryCollection(true)
    }

    fun saveDomainCollection(categoryCollection: CategoryCollection?) {
        if (categoryCollection != null) {
            dataRepository.saveCategoryCollection(categoryCollection)
        }
    }
}