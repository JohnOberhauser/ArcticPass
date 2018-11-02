package com.ober.arctic.ui

import androidx.lifecycle.ViewModel
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import javax.inject.Inject

class DataViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

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