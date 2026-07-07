package com.snehil.moon_stays_androidapp.core.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Flow<NetworkResult<T>> = flow {
    emit(NetworkResult.Loading)
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                emit(NetworkResult.Success(body))
            } else if (response.code() == 204 || response.code() == 200) {
                @Suppress("UNCHECKED_CAST")
                emit(NetworkResult.Success(Unit as T))
            } else {
                emit(NetworkResult.Error("Response body was empty"))
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: response.message()
            emit(NetworkResult.Error(errorMsg))
        }
    } catch (e: Exception) {
        emit(NetworkResult.Error(e.localizedMessage ?: "Unknown network error", e))
    }
}.flowOn(Dispatchers.IO)
