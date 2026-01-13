package com.kirya.everlastingmods.data.download

import android.content.Context
import android.net.Uri
import com.kirya.everlastingmods.data.fs.ZipInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class ModDownloadInstaller(
    private val context: Context,
    private val zipInstaller: ZipInstaller
) {
    private val client = OkHttpClient()

    suspend fun downloadAndInstall(
        rootUri: Uri,
        url: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val directUrl = DownloadResolver().resolve(url)

            val request = Request.Builder()
                .url(directUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Ошибка загрузки: HTTP ${response.code}")
                }

                val body = response.body ?: throw IllegalStateException("Пустой ответ сервера")

                val tempFile = File.createTempFile("mod_", ".zip", context.cacheDir)
                body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // ВАЖНО: ставим ZIP из File (не через contentResolver)
                val result = zipInstaller.installZipFromFile(rootUri, tempFile)

                tempFile.delete()

                result.getOrThrow()
            }
        }
    }
}
