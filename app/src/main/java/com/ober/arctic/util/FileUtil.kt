package com.ober.arctic.util

import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object FileUtil {

    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun writeStringToFile(fileName: String, content: String) {
        val file = File(Environment.getExternalStorageDirectory(), fileName)
        FileOutputStream(file).use {
            it.write(content.toByteArray())
        }
    }
}