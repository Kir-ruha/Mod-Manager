package com.kirya.everlastingmods.data.catalog

import android.content.Context
import com.kirya.everlastingmods.data.catalog.cache.CatalogCache
import com.kirya.everlastingmods.data.catalog.dto.CatalogDto
import com.kirya.everlastingmods.data.catalog.net.CatalogApi
import kotlinx.serialization.json.Json

class CatalogRepository(
    private val context: Context,
    private val api: CatalogApi
) {
    private val cache = CatalogCache(context)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val ttlMs = 6 * 60 * 60 * 1000L // 6 часов

    suspend fun getCatalog(forceRefresh: Boolean = false): CatalogDto {
        val (cachedJson, cachedAt) = cache.load()

        val cacheValid = cachedJson != null && cachedAt != null &&
                (System.currentTimeMillis() - cachedAt) < ttlMs

        if (!forceRefresh && cacheValid) {
            return json.decodeFromString(CatalogDto.serializer(), cachedJson!!)
        }

        // грузим из сети
        val catalog = api.getCatalog()

        // сохраняем в кэш (как JSON строку)
        val encoded = json.encodeToString(CatalogDto.serializer(), catalog)
        cache.save(encoded)

        return catalog
    }
}
