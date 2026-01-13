package com.kirya.everlastingmods.ui.screens

import androidx.documentfile.provider.DocumentFile
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@Composable
fun SelectGameFolderScreen(
    onFolderSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            if (isValidGameFolder(context, uri)) {
                takePersistablePermission(context, uri)
                onFolderSelected(uri)
            } else {
                error = "В выбранной папке не найдена папка 'mods'.\nВыберите папку игры, а не саму mods."
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Выберите папку игры",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Нужно выбрать папку, внутри которой находится папка mods.\n" +
                        "Пример: Android/media/.../Everlasting Summer/",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { picker.launch(null) }) {
                Text("Выбрать папку")
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
private fun isValidGameFolder(context: Context, treeUri: Uri): Boolean {
    val root = DocumentFile.fromTreeUri(context, treeUri)
        ?: return false

    return root.findFile("mods")?.isDirectory == true
}
private fun takePersistablePermission(context: Context, uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(uri, flags)
}
