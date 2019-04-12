package com.ober.arctic.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
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

class DataRepository @Inject constructor(
    private var mainDatabase: MainDatabase,
    private var keyManager: KeyManager,
    private var liveDataHolder: LiveDataHolder,
    private var gson: Gson,
    private var encryption: Encryption,
    private var appExecutors: AppExecutors
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
//        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(App.app)
//        if (googleSignInAccount != null) {
//            val fileContent = gson.toJson(encryptedDataHolder)
//            Drive.getDriveResourceClient(App.app!!, googleSignInAccount)?.let { driveResourceClient ->
//                val appFolderTask = driveResourceClient.appFolder
//                val createContentsTask = driveResourceClient.createContents()
//                val task = Tasks.whenAll(appFolderTask, createContentsTask)
//                    .continueWithTask {
//                        val parent = appFolderTask.result
//                        val contents = createContentsTask.result
//
//                        val outputStream = contents!!.outputStream
//                        val inputStream = ByteArrayInputStream(fileContent.toByteArray())
//
//                        inputStream.use { input ->
//                            outputStream.use { output ->
//                                input.copyTo(output)
//                            }
//                        }
//
//                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
//                        val changeSet = MetadataChangeSet.Builder()
//                            .setTitle("backup_" + simpleDateFormat.format(Date()))
//                            .setMimeType("application/octet-stream")
//                            .setStarred(true)
//                            .build()
//
//                        driveResourceClient.createFile(parent!!, changeSet, contents)
//                    }
//
//                task.addOnSuccessListener {
//                    println("success")
//                }
//                task.addOnFailureListener {
//                    println("failure")
//                    println(it)
//                }
//            }
//        }
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

    fun getCategoryCollectionLiveData(): LiveData<CategoryCollection> {
        return liveDataHolder.getCategoryCollection()
    }

    private inline fun <reified T> genericType() = object : TypeToken<T>() {}.type
}