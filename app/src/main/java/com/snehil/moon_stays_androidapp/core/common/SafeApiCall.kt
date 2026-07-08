package com.snehil.moon_stays_androidapp.core.common

import com.google.gson.JsonParser
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
            val rawError = response.errorBody()?.string()
            emit(NetworkResult.Error(parseErrorMessage(rawError) ?: response.message()))
        }
    } catch (e: Exception) {
        emit(NetworkResult.Error(e.localizedMessage ?: "Unknown network error", e))
    }
}.flowOn(Dispatchers.IO)

// Backend errors arrive as {"timeStamp":...,"data":null,"error":{"status":...,"message":...}}
private fun parseErrorMessage(rawError: String?): String? {
    if (rawError.isNullOrBlank()) return null
    return try {
        val root = JsonParser.parseString(rawError).asJsonObject
        root.getAsJsonObject("error")?.get("message")?.takeIf { it.isJsonPrimitive }?.asString
    } catch (e: Exception) {
        rawError
    }
}
