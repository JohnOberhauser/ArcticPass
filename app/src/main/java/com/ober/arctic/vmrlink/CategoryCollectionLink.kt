package com.ober.arctic.vmrlink

import androidx.lifecycle.LiveData
import com.ober.arctic.data.model.CategoryCollection
import com.ober.arctic.repository.DataRepository
import com.ober.vmrlink.Link
import com.ober.vmrlink.Resource
import com.ober.vmrlink.Source

class CategoryCollectionLink(private val dataRepository: DataRepository) : Link<CategoryCollection>() {

    fun save(categoryCollection: CategoryCollection?) {
        value.value = Resource.success(categoryCollection, Source.DATABASE)
        categoryCollection?.let {
            dataRepository.saveCategoryCollection(categoryCollection)
        }
    }

    override fun fetch(): LiveData<Resource<CategoryCollection>> {
        return dataRepository.loadCategoryCollection()
    }
}