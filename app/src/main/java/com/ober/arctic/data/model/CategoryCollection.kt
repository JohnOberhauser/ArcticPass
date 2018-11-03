package com.ober.arctic.data.model

class CategoryCollection(val categories: ArrayList<Category>) {

    fun getCategoryByName(name: String?): Category? {
        return categories.find { category -> category.name == name }
    }
}