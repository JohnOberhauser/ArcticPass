package com.ober.arctic.vmrlink

import androidx.lifecycle.LiveData
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import com.ober.vmrlink.LinkingLiveData
import com.ober.vmrlink.Resource
import com.ober.vmrlink.Source

class CategoryCollectionLink(private val dataRepository: DataRepository) : LinkingLiveData<CategoryCollection>() {

    fun save(categoryCollection: CategoryCollection?) {
        value = Resource.success(categoryCollection, Source.DATABASE)
        categoryCollection?.let {
            dataRepository.saveCategoryCollection(categoryCollection)
        }
    }

    fun clear() {
        value = Resource.loading(null, Source.NO_DATA)
    }

    override fun fetch(): LiveData<Resource<CategoryCollection>> {
        return dataRepository.loadCategoryCollection()
    }
}