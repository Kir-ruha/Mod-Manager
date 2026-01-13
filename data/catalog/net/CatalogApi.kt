package com.kirya.everlastingmods.data.catalog.net

import com.kirya.everlastingmods.data.catalog.dto.CatalogDto
import retrofit2.http.GET

interface CatalogApi {
    @GET("catalog.json")
    suspend fun getCatalog(): CatalogDto
}
