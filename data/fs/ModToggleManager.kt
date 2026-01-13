package com.kirya.everlastingmods.data.fs

import android.content.Context
import android.net.Uri

class ModToggleManager(
    private val context: Context,
    private val fs: SafModFs
) {

    fun enableMod(rootUri: Uri, folderName: String) {
        val dirs = fs.prepare(rootUri)
        val src = dirs.disabled.findFile(folderName) ?: return
        moveDir(src, dirs.mods)
    }

    fun disableMod(rootUri: Uri, folderName: String) {
        val dirs = fs.prepare(rootUri)
        val src = dirs.mods.findFile(folderName) ?: return
        moveDir(src, dirs.disabled)
    }

    private fun moveDir(src: androidx.documentfile.provider.DocumentFile, dstParent: androidx.documentfile.provider.DocumentFile) {
        val dst = dstParent.createDirectory(src.name ?: return) ?: return

        src.listFiles().forEach { file ->
            if (file.isDirectory) {
                val sub = dst.createDirectory(file.name ?: "") ?: return@forEach
                moveDir(file, sub)
            } else {
                val newFile = dst.createFile(file.type ?: "application/octet-stream", file.name ?: "") ?: return@forEach
                context.contentResolver.openInputStream(file.uri)?.use { input ->
                    context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        src.delete()
    }
}
