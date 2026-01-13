package com.kirya.everlastingmods.data.download.yandex

import retrofit2.http.GET
import retrofit2.http.Query

interface YandexDiskApi {

    @GET("v1/disk/public/resources/download")
    suspend fun getDownloadLink(
        @Query("public_key") publicKey: String
    ): YandexDownloadDto
}
