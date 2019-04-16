package com.ober.arctic.ui

import androidx.lifecycle.ViewModel
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import com.ober.arctic.vmrlink.BackupFilesLink
import javax.inject.Inject

class DataViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val domainCollectionLiveData = dataRepository.getCategoryCollectionLiveData()

    var backupFilesLink = BackupFilesLink(dataRepository)

    fun loadDomainCollection() {
        dataRepository.loadCategoryCollection(true)
    }

    fun saveDomainCollection(categoryCollection: CategoryCollection?) {
        if (categoryCollection != null) {
            dataRepository.saveCategoryCollection(categoryCollection)
        }
    }
}