package com.example.share_quote_app.data

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val exception: Throwable) : AuthResult<Nothing>()
}
