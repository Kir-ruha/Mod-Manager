package com.kirya.everlastingmods.ui.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberZipPicker(
    onPicked: (Uri) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
    onResult = { uri ->
        if (uri != null) onPicked(uri)
    }
)
