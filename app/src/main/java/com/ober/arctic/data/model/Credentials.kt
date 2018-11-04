package com.ober.arctic.data.model

class Credentials {
    var description: String? = null
    var website: String? = null
    var username: String? = null
    var password: String? = null
    var notes: String? = null
}

class CredentialsComparator : Comparator<Credentials> {
    override fun compare(credentials1: Credentials?, credentials2: Credentials?): Int {
        return compareValues(credentials1!!.description?.toLowerCase(), credentials2!!.description?.toLowerCase())
    }
}