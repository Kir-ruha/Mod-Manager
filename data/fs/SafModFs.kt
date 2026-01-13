package com.kirya.everlastingmods.data.fs

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

data class ModDirs(
    val root: DocumentFile,
    val mods: DocumentFile,
    val disabled: DocumentFile,
    val temp: DocumentFile
)

class SafModFs(private val context: Context) {

    fun prepare(rootTreeUri: Uri): ModDirs {
        val root = DocumentFile.fromTreeUri(context, rootTreeUri)
            ?: error("Bad treeUri")

        val mods = root.findDir("mods")
            ?: error("В выбранной папке нет папки mods")

        val disabled = root.findOrCreateDir("mods_disabled")
        val temp = root.findOrCreateDir("temp")

        return ModDirs(
            root = root,
            mods = mods,
            disabled = disabled,
            temp = temp
        )
    }
    fun moveDir(src: DocumentFile, dstParent: DocumentFile) {
        val dst = dstParent.createDirectory(src.name ?: return) ?: return

        src.listFiles().forEach { file ->
            if (file.isDirectory) {
                val sub = dst.createDirectory(file.name ?: "") ?: return@forEach
                moveDir(file, sub)
            } else {
                val newFile = dst.createFile(
                    file.type ?: "application/octet-stream",
                    file.name ?: ""
                ) ?: return@forEach

                context.contentResolver.openInputStream(file.uri)?.use { input ->
                    context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        src.delete()
    }

    private fun DocumentFile.findDir(name: String): DocumentFile? =
        findFile(name)?.takeIf { it.isDirectory }

    private fun DocumentFile.findOrCreateDir(name: String): DocumentFile =
        findDir(name) ?: createDirectory(name)
        ?: error("Не удалось создать папку $name")
}
