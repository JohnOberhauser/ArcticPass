package com.ober.arctic.util

import com.google.api.services.drive.Drive

interface DriveServiceHolder {
    fun setDriveService(drive: Drive)
    fun getDriveService(): Drive?
}

class DriveServiceHolderImpl : DriveServiceHolder {
    private var drive: Drive? = null

    override fun setDriveService(drive: Drive) {
        this.drive = drive
    }

    override fun getDriveService(): Drive? {
        return drive
    }
}