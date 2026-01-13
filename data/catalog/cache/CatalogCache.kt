package com.kirya.everlastingmods.data.catalog.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.catalogStore by preferencesDataStore("catalog_cache")

class CatalogCache(private val context: Context) {
    private val KEY_JSON = stringPreferencesKey("catalog_json")
    private val KEY_TIME = longPreferencesKey("catalog_cached_at")

    suspend fun save(json: String) {
        context.catalogStore.edit { prefs ->
            prefs[KEY_JSON] = json
            prefs[KEY_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun load(): Pair<String?, Long?> {
        val data = context.catalogStore.data.first()
        return data[KEY_JSON] to data[KEY_TIME]
    }

    suspend fun clear() {
        context.catalogStore.edit { it.clear() }
    }
}
