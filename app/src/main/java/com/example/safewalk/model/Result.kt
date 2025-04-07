package com.example.safewalk.model

/**
 * A sealed class representing the result of an operation that can either succeed or fail.
 * @param T The type of data that can be returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful result with data
     * @param data The data returned by the successful operation
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Represents a failed result with an error
     * @param exception The exception that caused the failure
     */
    data class Failure(val exception: Exception) : Result<Nothing>()
    
    /**
     * Returns true if this result is a Success
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Returns true if this result is a Failure
     */
    fun isFailure(): Boolean = this is Failure
    
    /**
     * Returns the data if this is a Success, or null if this is a Failure
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }
    
    /**
     * Returns the exception if this is a Failure, or null if this is a Success
     */
    fun exceptionOrNull(): Exception? = when (this) {
        is Success -> null
        is Failure -> exception
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw exception
    }
    
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> Failure(exception)
    }
    
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    fun onFailure(action: (Exception) -> Unit): Result<T> {
        if (this is Failure) {
            action(exception)
        }
        return this
    }
    
    companion object {
        /**
         * Creates a Success result with the given data
         */
        fun <T> success(data: T): Result<T> = Success(data)
        
        /**
         * Creates a Failure result with the given exception
         */
        fun <T> failure(exception: Exception): Result<T> = Failure(exception)
    }
} 