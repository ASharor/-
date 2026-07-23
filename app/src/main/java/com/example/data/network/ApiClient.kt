package com.example.data.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    val gson = Gson()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    inline fun <reified T> parseRecordList(jsonString: String): List<T> {
        return try {
            val jsonElement = gson.fromJson(jsonString, com.google.gson.JsonElement::class.java)
            val recordElement = if (jsonElement.isJsonObject && jsonElement.asJsonObject.has("record")) {
                jsonElement.asJsonObject.get("record")
            } else {
                jsonElement
            }
            if (recordElement.isJsonArray) {
                val type = object : TypeToken<List<T>>() {}.type
                gson.fromJson(recordElement, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
