package com.ober.arctic.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ober.arctic.App
import com.ober.arctic.data.cache.LiveDataHolder
import com.ober.arctic.data.database.MainDatabase
import com.ober.arctic.data.model.Category
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.data.model.EncryptedDataHolder
import com.ober.arctic.util.AppExecutors
import com.ober.arctic.util.security.Encryption
import com.ober.arctic.util.security.KeyManager
import com.ober.arcticpass.R
import javax.inject.Inject
import java.util.Collections.singletonList
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.ober.arctic.util.DriveServiceHolder
import java.text.SimpleDateFormat
import java.util.*


class DataRepository @Inject constructor(
    private var mainDatabase: MainDatabase,
    private var keyManager: KeyManager,
    private var liveDataHolder: LiveDataHolder,
    private var gson: Gson,
    private var encryption: Encryption,
    private var appExecutors: AppExecutors,
    private var driveServiceHolder: DriveServiceHolder
) {

    @SuppressLint("SimpleDateFormat")
    fun saveCategoryCollection(categoryCollection: CategoryCollection) {
        val encryptedDataHolder: EncryptedDataHolder =
            encryption.encryptStringData(gson.toJson(categoryCollection), keyManager.getEncyptionKey()!!)
        appExecutors.diskIO().execute {
            mainDatabase.encryptedDataHolderDao().insert(encryptedDataHolder)
            appExecutors.mainThread().execute {
                liveDataHolder.setCategoryCollection(categoryCollection)
            }
        }

        createFile(gson.toJson(encryptedDataHolder))
    }

    fun loadCategoryCollection(createDefaultsIfNecessary: Boolean) {
        if (liveDataHolder.getCategoryCollection().value == null) {
            val source = mainDatabase.encryptedDataHolderDao().getEncryptedDataHolder()
            liveDataHolder.getCategoryCollectionLiveData().addSource(source) { encryptedDataHolder ->
                liveDataHolder.getCategoryCollectionLiveData().removeSource(source)
                when {
                    encryptedDataHolder != null -> {
                        appExecutors.miscellaneousThread().execute {
                            val categoryCollection: CategoryCollection = gson.fromJson(
                                encryption.decryptStringData(
                                    encryptedDataHolder.encryptedJson,
                                    encryptedDataHolder.salt,
                                    keyManager.getEncyptionKey()!!
                                ),
                                genericType<CategoryCollection>()
                            )
                            appExecutors.mainThread().execute {
                                liveDataHolder.setCategoryCollection(categoryCollection)
                            }
                        }
                    }
                    createDefaultsIfNecessary -> {
                        val domainList = arrayListOf<Category>()
                        domainList.add(Category(App.app!!.getString(R.string.business), arrayListOf()))
                        domainList.add(Category(App.app!!.getString(R.string.personal), arrayListOf()))
                        val domainCollection = CategoryCollection(domainList)
                        saveCategoryCollection(domainCollection)
                    }
                    else -> appExecutors.mainThread().execute {
                        liveDataHolder.setCategoryCollection(null)
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun createFile(content: String) {
        driveServiceHolder.getDriveService()?.let { drive ->
            appExecutors.networkIO().execute {
                val folderId = getFolderId(drive)

                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                val fileMetaData = File()
                    .setParents(singletonList(folderId))
                    .setMimeType("text/plain")
                    .setName(App.app!!.getString(R.string.backup) + simpleDateFormat.format(Date()))

                val inputStream = ByteArrayContent.fromString("text/plain", content)

                drive.files().create(fileMetaData, inputStream).execute()
            }
        }

    }

    private fun getFolderId(drive: Drive): String? {
        val list = drive.files().list().execute()
        for (file in list.files) {
            if (file.name == App.app!!.getString(R.string.folder_name)) {
                return file.id
            }
        }
        return createFolder(drive)
    }

    private fun createFolder(drive: Drive): String? {
        val folderMetaData = File()
            .setName(App.app!!.getString(R.string.folder_name))
            .setMimeType("application/vnd.google-apps.folder")
        val folder = drive.files().create(folderMetaData)
            .setFields("id")
            .execute()

        return folder.id
    }

    fun getBackupFiles(): MutableLiveData<List<File>> {
        val liveData = MutableLiveData<List<File>>()
        driveServiceHolder.getDriveService()?.let { drive ->
            appExecutors.networkIO().execute {
                val files = arrayListOf<File>()
                val list = drive.files().list().execute()
                for (file in list.files) {
                    if (file.name.contains(App.app!!.getString(R.string.backup))) {
                        files.add(file)
                    }
                }
                appExecutors.mainThread().execute {
                    liveData.value = files
                }
            }
        }
        return liveData
    }

    fun getCategoryCollectionLiveData(): LiveData<CategoryCollection> {
        return liveDataHolder.getCategoryCollection()
    }

    private inline fun <reified T> genericType() = object : TypeToken<T>() {}.type
}