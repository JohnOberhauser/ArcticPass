package com.ober.arctic.vmrlink

import androidx.lifecycle.LiveData
import com.google.api.services.drive.model.File
import com.ober.arctic.repository.DataRepository
import com.ober.vmrlink.Resource
import com.ober.vmrlink.SimpleLink

class BackupFilesLink(private val dataRepository: DataRepository) : SimpleLink<List<File>>() {

    override fun fetch(): LiveData<Resource<List<File>>> {
        return dataRepository.getBackupFiles()
    }
}