package com.ober.arctic.data.model

class Category(
    var name: String,
    val credentialsList: ArrayList<Credentials>
)

class CategoryComparator : Comparator<Category> {
    override fun compare(category1: Category?, category2: Category?): Int {
        return compareValues(category1!!.name.toLowerCase(), category2!!.name.toLowerCase())
    }
}