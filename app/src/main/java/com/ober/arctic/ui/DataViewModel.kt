package com.ober.arctic.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.services.drive.model.File
import com.ober.arctic.repository.DataRepository
import com.ober.arctic.vmrlink.BackupFilesLink
import com.ober.arctic.vmrlink.CategoryCollectionLink
import com.ober.vmrlink.Resource
import javax.inject.Inject

class DataViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val categoryCollectionLink = CategoryCollectionLink(dataRepository)

    val backupFilesLink = BackupFilesLink(dataRepository)

    fun getSingleFileBackup(file: File): MutableLiveData<Resource<String>> {
        return dataRepository.getSingleBackupFile(file)
    }
}