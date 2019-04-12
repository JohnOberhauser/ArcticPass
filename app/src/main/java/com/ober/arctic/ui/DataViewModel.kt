package com.ober.arctic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.api.services.drive.model.File
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import javax.inject.Inject

class DataViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val domainCollectionLiveData = dataRepository.getCategoryCollectionLiveData()

    var backupFiles = MutableLiveData<List<File>>()
    private var backupFilesMediatorLiveData: LiveData<List<File>>? = null

    fun loadDomainCollection() {
        dataRepository.loadCategoryCollection(true)
    }

    fun saveDomainCollection(categoryCollection: CategoryCollection?) {
        if (categoryCollection != null) {
            dataRepository.saveCategoryCollection(categoryCollection)
        }
    }

    fun updateBackupFiles() {
        if (backupFilesMediatorLiveData != null && backupFilesMediatorLiveData!!.hasObservers()) {
            return
        }
        backupFilesMediatorLiveData = dataRepository.getBackupFiles()
        backupFilesMediatorLiveData?.observeForever(object : Observer<List<File>> {
            override fun onChanged(it: List<File>?) {
                backupFiles.value = it
                backupFilesMediatorLiveData?.removeObserver(this)
            }
        })
    }
}