package com.example.quoteverse.core.result

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    data class Loading(val message: String? = null) : Resource<Nothing>()
}
