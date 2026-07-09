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
                emit(NetworkResult.Error(Exception("Response body was empty"), "Response body was empty"))
            }
        } else {
            val rawError = response.errorBody()?.string()
            val errorMsg = parseErrorMessage(rawError) ?: response.message()
            emit(NetworkResult.Error(Exception(errorMsg), errorMsg))
        }
    } catch (e: Exception) {
        emit(NetworkResult.Error(e, e.localizedMessage ?: "Unknown network error"))
    }
}.flowOn(Dispatchers.IO)

// Backend errors arrive as {"timeStamp":...,"data":null,"error":{"status":...,"message":...}}
// Or sometimes wrapped validation errors inside the "data" field
private fun parseErrorMessage(rawError: String?): String? {
    if (rawError.isNullOrBlank()) return null
    return try {
        val root = JsonParser.parseString(rawError).asJsonObject
        
        // 1. Check if root has "error" object
        if (root.has("error") && root.get("error").isJsonObject) {
            val errorObj = root.getAsJsonObject("error")
            if (errorObj.has("message") && errorObj.get("message").isJsonPrimitive) {
                return errorObj.get("message").asString
            }
        }
        
        // 2. Check if root has "data" object (e.g. validation error wrapped inside data)
        if (root.has("data") && root.get("data").isJsonObject) {
            val dataObj = root.getAsJsonObject("data")
            if (dataObj.has("message") && dataObj.get("message").isJsonPrimitive) {
                return dataObj.get("message").asString
            }
        }
        
        // 3. Check if root has "message" directly
        if (root.has("message") && root.get("message").isJsonPrimitive) {
            return root.get("message").asString
        }
        
        null
    } catch (e: Exception) {
        null
    }
}
