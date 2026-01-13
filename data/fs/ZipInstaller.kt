package com.kirya.everlastingmods.data.fs

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

class ZipInstaller(
    private val context: Context,
    private val fs: SafModFs
) {

    fun installZip(rootUri: Uri, zipUri: Uri): Result<String> {
        val input = context.contentResolver.openInputStream(zipUri)
            ?: return Result.failure(Exception("Не удалось открыть ZIP"))
        input.use { return installZipFromInputStream(rootUri, it) }
    }

    fun installZipFromFile(rootUri: Uri, zipFile: File): Result<String> {
        zipFile.inputStream().use { return installZipFromInputStream(rootUri, it) }
    }

    private fun installZipFromInputStream(rootUri: Uri, input: InputStream): Result<String> {
        val dirs = fs.prepare(rootUri)
        val tempDir = dirs.temp

        // очищаем temp
        tempDir.listFiles().forEach { it.delete() }

        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val parts = entry.name.split("/").filter { it.isNotBlank() }
                if (parts.isNotEmpty()) {
                    var current: DocumentFile = tempDir

                    for (i in parts.indices) {
                        val name = parts[i]
                        val isLast = i == parts.lastIndex

                        current = if (isLast && !entry.isDirectory) {
                            current.createFile("application/octet-stream", name)
                                ?: return Result.failure(Exception("Не удалось создать файл: $name"))
                        } else {
                            current.findFile(name)
                                ?: current.createDirectory(name)
                                ?: return Result.failure(Exception("Не удалось создать папку: $name"))
                        }
                    }

                    if (!entry.isDirectory) {
                        context.contentResolver.openOutputStream(current.uri)?.use { out ->
                            zip.copyTo(out)
                        } ?: return Result.failure(Exception("Не удалось записать файл из архива"))
                    }
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        // проверка: одна папка в корне temp
        val folders = tempDir.listFiles().filter { it.isDirectory }
        if (folders.size != 1) {
            return Result.failure(Exception("ZIP должен содержать одну папку с модом"))
        }

        val modFolder = folders.first()
        val modName = modFolder.name ?: "unknown"

        // если мод уже есть — удаляем
        dirs.mods.findFile(modName)?.delete()

        // переносим из temp в mods (SAF-safe copy+delete)
        moveDir(modFolder, dirs.mods)

        return Result.success(modName)
    }

    private fun moveDir(src: DocumentFile, dstParent: DocumentFile) {
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
}
