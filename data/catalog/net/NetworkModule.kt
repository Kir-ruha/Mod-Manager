package com.kirya.everlastingmods.data.catalog.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object NetworkModule {

    fun createCatalogApi(baseUrl: String): CatalogApi {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl) // важно: baseUrl должен оканчиваться на "/"
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(CatalogApi::class.java)
    }
}
