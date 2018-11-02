package com.ober.arctic.repository

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ober.arctic.App
import com.ober.arctic.data.cache.LiveDataHolder
import com.ober.arctic.data.database.MainDatabase
import com.ober.arctic.data.model.Domain
import com.ober.arctic.data.model.DomainCollection
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

    fun saveDomainCollection(domainCollection: DomainCollection) {
        val encryptedDataHolder = encryption.encryptString(gson.toJson(domainCollection), keyManager.getCombinedKey()!!)
        appExecutors.diskIO().execute {
            mainDatabase.encryptedDataHolderDao().insert(encryptedDataHolder)
            appExecutors.mainThread().execute {
                liveDataHolder.setDomainCollection(domainCollection)
            }
        }
    }

    fun loadDomainCollection(createDefaultsIfNecessary: Boolean) {
        if (liveDataHolder.getDomainCollection().value == null) {
            val source = mainDatabase.encryptedDataHolderDao().getEncryptedDataHolder()
            liveDataHolder.getDomainCollectionLiveData().addSource(source) { encryptedDataHolder ->
                liveDataHolder.getDomainCollectionLiveData().removeSource(source)
                when {
                    encryptedDataHolder != null -> {
                        appExecutors.miscellaneousThread().execute {
                            val domainCollection: DomainCollection = gson.fromJson(
                                encryption.decryptString(
                                    encryptedDataHolder.encryptedJson,
                                    encryptedDataHolder.salt,
                                    keyManager.getCombinedKey()!!
                                ),
                                genericType<DomainCollection>()
                            )
                            appExecutors.mainThread().execute {
                                liveDataHolder.setDomainCollection(domainCollection)
                            }
                        }
                    }
                    createDefaultsIfNecessary -> {
                        val domainList = arrayListOf<Domain>()
                        domainList.add(Domain(App.app!!.getString(R.string.business), arrayListOf()))
                        domainList.add(Domain(App.app!!.getString(R.string.personal), arrayListOf()))
                        val domainCollection = DomainCollection(domainList)
                        saveDomainCollection(domainCollection)
                    }
                    else -> appExecutors.mainThread().execute {
                        liveDataHolder.setDomainCollection(null)
                    }
                }
            }
        }
    }

    fun getDomainCollectionLiveData(): LiveData<DomainCollection> {
        return liveDataHolder.getDomainCollection()
    }

    private inline fun <reified T> genericType() = object : TypeToken<T>() {}.type
}