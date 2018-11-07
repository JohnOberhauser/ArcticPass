package com.ober.arctic.util

import com.google.gson.reflect.TypeToken

object TypeUtil {
    inline fun <reified T> genericType() = object : TypeToken<T>() {}.type
}