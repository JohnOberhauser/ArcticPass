package com.ober.arctic.data.cache

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.ober.arctic.data.model.CategoryCollection

interface LiveDataHolder {
    fun setCategoryCollection(categoryCollection: CategoryCollection?)
    fun getCategoryCollection(): LiveData<CategoryCollection>
    fun getCategoryCollectionLiveData(): MediatorLiveData<CategoryCollection>
}

class LiveDataHolderImpl : LiveDataHolder {

    private var mediatorLiveData = MediatorLiveData<CategoryCollection>()

    override fun getCategoryCollectionLiveData(): MediatorLiveData<CategoryCollection> {
        return mediatorLiveData
    }

    override fun setCategoryCollection(categoryCollection: CategoryCollection?) {
        mediatorLiveData.value = categoryCollection
    }

    override fun getCategoryCollection(): LiveData<CategoryCollection> {
        return mediatorLiveData
    }
}