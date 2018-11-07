package com.ober.arctic.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

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

    fun readTextFromUri(uri: Uri, context: Context): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
}