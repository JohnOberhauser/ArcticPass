package com.ober.arctic.repository

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

    fun saveCategoryCollection(categoryCollection: CategoryCollection) {
        val encryptedDataHolder: EncryptedDataHolder =
            encryption.encryptStringData(gson.toJson(categoryCollection), keyManager.getEncyptionKey()!!)
        appExecutors.diskIO().execute {
            mainDatabase.encryptedDataHolderDao().insert(encryptedDataHolder)
            appExecutors.mainThread().execute {
                liveDataHolder.setCategoryCollection(categoryCollection)
            }
        }
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