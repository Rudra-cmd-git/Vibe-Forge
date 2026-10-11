package com.vibecoder.util

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun <T> failure(error: Throwable): Result<T> = Failure(error)
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> Failure(error)
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> Failure(error)
    }

    fun onSuccess(action: (T) -> Unit): Result<T> = when (this) {
        is Success -> {
            action(value)
            this
        }
        is Failure -> this
    }

    fun onFailure(action: (Throwable) -> Unit): Result<T> = when (this) {
        is Success -> this
        is Failure -> {
            action(error)
            this
        }
    }

    fun getOrElse(default: T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}