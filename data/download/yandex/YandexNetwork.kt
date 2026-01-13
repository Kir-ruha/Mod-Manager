package com.kirya.everlastingmods.data.download.yandex

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object YandexNetwork {

    fun createApi(): YandexDiskApi {
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl("https://cloud-api.yandex.net/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(YandexDiskApi::class.java)
    }
}
