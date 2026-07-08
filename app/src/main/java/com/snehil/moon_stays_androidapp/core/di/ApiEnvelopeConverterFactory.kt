package com.snehil.moon_stays_androidapp.core.di

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * The backend wraps every successful response in an envelope produced by its
 * GlobalResponseHandler: {"timeStamp": "...", "data": <payload>}.
 * API service interfaces declare the payload type directly, so this converter
 * extracts the "data" node before Gson deserialization. Non-enveloped bodies
 * are deserialized as-is.
 */
class ApiEnvelopeConverterFactory(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, Any?> { body ->
            body.use {
                val root = JsonParser.parseString(it.string())
                val payload = if (root.isJsonObject &&
                    root.asJsonObject.has("timeStamp") &&
                    root.asJsonObject.has("data")
                ) {
                    root.asJsonObject.get("data")
                } else {
                    root
                }
                gson.fromJson(payload, type)
            }
        }
    }
}
