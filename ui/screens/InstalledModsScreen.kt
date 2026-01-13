package com.kirya.everlastingmods.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kirya.everlastingmods.data.fs.ModToggleManager
import com.kirya.everlastingmods.data.fs.SafModFs
import com.kirya.everlastingmods.data.fs.ZipInstaller
import com.kirya.everlastingmods.data.model.InstalledMod
import com.kirya.everlastingmods.data.repository.InstalledModsRepository
import com.kirya.everlastingmods.ui.util.rememberZipPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledModsScreen(
    rootUri: Uri,
    onChangeFolder: () -> Unit
) {
    val context = LocalContext.current

    // filesystem / logic
    val fs = remember { SafModFs(context) }
    val repo = remember { InstalledModsRepository(fs) }
    val toggle = remember { ModToggleManager(context, fs) }
    val installer = remember { ZipInstaller(context, fs) }

    var mods by remember { mutableStateOf<List<InstalledMod>>(emptyList()) }

    fun reload() {
        mods = repo.loadInstalledMods(rootUri)
    }

    // ZIP picker
    val zipPicker = rememberZipPicker { zipUri ->
        val result = installer.installZip(rootUri, zipUri)
        if (result.isSuccess) {
            reload()
        }
    }

    LaunchedEffect(rootUri) {
        reload()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Установленные моды") },
                actions = {
                    IconButton(onClick = onChangeFolder) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = "Сменить папку игры"
                        )
                    }
                    IconButton(
                        onClick = {
                            zipPicker.launch(arrayOf("application/zip"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Установить мод из ZIP"
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (mods.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Моды не найдены")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(mods) { mod ->
                    ModItem(
                        mod = mod,
                        onToggle = {
                            if (mod.enabled) {
                                toggle.disableMod(rootUri, mod.folderName)
                            } else {
                                toggle.enableMod(rootUri, mod.folderName)
                            }
                            reload()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModItem(
    mod: InstalledMod,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = mod.folderName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (mod.enabled) "Включён" else "Выключен",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(onClick = onToggle) {
                Text(if (mod.enabled) "Выключить" else "Включить")
            }
        }
    }
}
