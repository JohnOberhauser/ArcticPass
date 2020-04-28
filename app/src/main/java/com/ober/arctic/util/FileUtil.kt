package com.ober.arctic.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.ober.arctic.App
import com.ober.arcticpass.R
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.ParseException
import java.util.*

object FileUtil {

    private const val JSON_EXTENSION = ".json"

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

    fun buildFileName(): String {
        return App.app!!.getString(R.string.backup) + DateFormat.dateFormat.format(Date()) + JSON_EXTENSION
    }

    fun getDateFromFileName(fileName: String): Date? {
        return try {
            DateFormat.dateFormat.parse(
                fileName
                    .substringAfter(App.app!!.getString(R.string.backup))
                    .substringBefore(JSON_EXTENSION)
            )
        } catch (e: ParseException) {
            null
        }
    }
}