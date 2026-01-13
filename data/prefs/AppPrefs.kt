package com.kirya.everlastingmods.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

class AppPrefs(private val context: Context) {

    private val KEY_ROOT_URI = stringPreferencesKey("root_tree_uri")

    val rootTreeUri: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_ROOT_URI] }

    suspend fun setRootTreeUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROOT_URI] = uri
        }
    }

    suspend fun clearRootTreeUri() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ROOT_URI)
        }
    }
}
