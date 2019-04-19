package com.ober.arctic.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.services.drive.model.File
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import com.ober.arctic.vmrlink.BackupFilesLink
import com.ober.vmrlink.Resource
import javax.inject.Inject

class DataViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val categoryCollectionLiveData = dataRepository.getCategoryCollectionLiveData()

    var backupFilesLink = BackupFilesLink(dataRepository)

    fun loadCategoryCollection() {
        dataRepository.loadCategoryCollection(true)
    }

    fun saveCategoryCollection(categoryCollection: CategoryCollection?) {
        if (categoryCollection != null) {
            dataRepository.saveCategoryCollection(categoryCollection)
        }
    }

    fun getSingleFileBackup(file: File): MutableLiveData<Resource<String>> {
        return dataRepository.getSingleBackupFile(file)
    }
}