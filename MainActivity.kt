package com.kirya.everlastingmods

import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.kirya.everlastingmods.data.prefs.AppPrefs
import com.kirya.everlastingmods.ui.screens.CatalogScreen
import com.kirya.everlastingmods.ui.screens.InstalledModsScreen
import com.kirya.everlastingmods.ui.screens.SelectGameFolderScreen
import com.kirya.everlastingmods.ui.theme.EverlastingModsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EverlastingModsTheme {
                val prefs = remember { AppPrefs(this) }
                val scope = rememberCoroutineScope()

                val savedUriString by prefs.rootTreeUri.collectAsState(initial = null)
                val savedUri = savedUriString?.let { runCatching { Uri.parse(it) }.getOrNull() }

                // 0 = Установленные, 1 = Каталог
                var tab by remember { mutableStateOf(0) }

                if (savedUri == null) {
                    SelectGameFolderScreen(
                        onFolderSelected = { uri ->
                            scope.launch { prefs.setRootTreeUri(uri.toString()) }
                        }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            TabRow(selectedTabIndex = tab) {
                                Tab(
                                    selected = tab == 0,
                                    onClick = { tab = 0 },
                                    text = { Text("Установленные") }
                                )
                                Tab(
                                    selected = tab == 1,
                                    onClick = { tab = 1 },
                                    text = { Text("Каталог") }
                                )
                            }
                        }
                    ) { padding ->
                        // Чтобы экраны не прилипали под TabRow
                        Surface(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                            when (tab) {
                                0 -> InstalledModsScreen(
                                    rootUri = savedUri,
                                    onChangeFolder = {
                                        scope.launch { prefs.clearRootTreeUri() }
                                    }
                                )
                                1 -> CatalogScreen(
                                    baseUrl = "https://raw.githubusercontent.com/Kir-ruha/everlasting-mods/main/",
                                    rootUri = savedUri
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}
