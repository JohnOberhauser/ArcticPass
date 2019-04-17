package com.ober.arctic.vmrlink

import androidx.lifecycle.LiveData
import com.google.api.services.drive.model.File
import com.ober.arctic.repository.DataRepository
import com.ober.vmrlink.Link
import com.ober.vmrlink.Resource

class BackupFilesLink(private val dataRepository: DataRepository) : Link<List<File>>() {

    override fun fetch(): LiveData<Resource<List<File>>> {
        return dataRepository.getBackupFiles()
    }
}