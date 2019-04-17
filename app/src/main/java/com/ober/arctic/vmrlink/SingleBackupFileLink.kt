package com.ober.arctic.vmrlink

import androidx.lifecycle.LiveData
import com.google.api.services.drive.model.File
import com.ober.arctic.repository.DataRepository
import com.ober.vmrlink.Link
import com.ober.vmrlink.Resource

class SingleBackupFileLink(private val dataRepository: DataRepository) : Link<String>() {

    var file: File? = null

    fun update(file: File) {
        this.file = file
        update()
    }

    override fun fetch(): LiveData<Resource<String>> {
        return dataRepository.getSingleBackupFile(file!!)
    }
}