package com.kirya.everlastingmods.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kirya.everlastingmods.data.catalog.CatalogRepository
import com.kirya.everlastingmods.data.catalog.dto.ModDto
import com.kirya.everlastingmods.data.catalog.net.NetworkModule
import com.kirya.everlastingmods.data.download.ModDownloadInstaller
import com.kirya.everlastingmods.data.fs.SafModFs
import com.kirya.everlastingmods.data.fs.ZipInstaller
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    baseUrl: String,
    rootUri: Uri
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // network + repo
    val api = remember(baseUrl) { NetworkModule.createCatalogApi(baseUrl) }
    val repo = remember(baseUrl) { CatalogRepository(context, api) }

    // installer
    val fs = remember { SafModFs(context) }
    val zipInstaller = remember { ZipInstaller(context, fs) }
    val downloader = remember { ModDownloadInstaller(context, zipInstaller) }

    var state by remember { mutableStateOf("loading") }
    var mods by remember { mutableStateOf<List<ModDto>>(emptyList()) }

    LaunchedEffect(baseUrl) {
        runCatching {
            repo.getCatalog(forceRefresh = true)
        }.onSuccess { catalog ->
            mods = catalog.mods
            state = "ok"
        }.onFailure {
            state = "error: ${it.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Каталог") })
        }
    ) { padding ->

        when {
            state == "loading" -> {
                Text(
                    "Загрузка…",
                    modifier = Modifier.padding(padding)
                )
            }

            state.startsWith("error") -> {
                Text(
                    state,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    items(mods) { mod ->
                        CatalogModItem(
                            mod = mod,
                            onDownload = {
                                scope.launch {
                                    downloader.downloadAndInstall(
                                        rootUri = rootUri,
                                        url = mod.downloadUrl
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogModItem(
    mod: ModDto,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(mod.title, style = MaterialTheme.typography.titleMedium)
            Text("Автор: ${mod.author}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onDownload) {
                Text("Скачать")
            }
        }
    }
}
