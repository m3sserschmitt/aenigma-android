package com.example.enigma.util

sealed class DatabaseRequestState<out T> {
    data object Idle: DatabaseRequestState<Nothing>()
    data object Loading: DatabaseRequestState<Nothing>()
    data class Success<T>(val data: T): DatabaseRequestState<T>()
    data class Error(val error: Throwable): DatabaseRequestState<Nothing>()
}
