package com.ober.arctic.data.cache

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.ober.arctic.data.model.DomainCollection

interface LiveDataHolder {
    fun setDomainCollection(domainCollection: DomainCollection?)
    fun getDomainCollection(): LiveData<DomainCollection>
    fun getDomainCollectionLiveData(): MediatorLiveData<DomainCollection>
}

class LiveDataHolderImpl : LiveDataHolder {

    private var mediatorLiveData = MediatorLiveData<DomainCollection>()

    override fun getDomainCollectionLiveData(): MediatorLiveData<DomainCollection> {
        return mediatorLiveData
    }

    override fun setDomainCollection(domainCollection: DomainCollection?) {
        mediatorLiveData.value = domainCollection
    }

    override fun getDomainCollection(): LiveData<DomainCollection> {
        return mediatorLiveData
    }
}