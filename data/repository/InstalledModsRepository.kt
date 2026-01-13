package com.kirya.everlastingmods.data.repository

import android.net.Uri
import com.kirya.everlastingmods.data.fs.SafModFs
import com.kirya.everlastingmods.data.model.InstalledMod

class InstalledModsRepository(
    private val fs: SafModFs
) {

    fun loadInstalledMods(rootUri: Uri): List<InstalledMod> {
        val dirs = fs.prepare(rootUri)

        val enabledMods = dirs.mods.listFiles()
            .filter { it.isDirectory }
            .map { InstalledMod(it.name ?: "unknown", enabled = true) }

        val disabledMods = dirs.disabled.listFiles()
            .filter { it.isDirectory }
            .map { InstalledMod(it.name ?: "unknown", enabled = false) }

        return (enabledMods + disabledMods)
            .sortedBy { it.folderName.lowercase() }
    }
}
