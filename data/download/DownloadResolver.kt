package com.kirya.everlastingmods.data.download

import com.kirya.everlastingmods.data.download.yandex.YandexNetwork

class DownloadResolver {

    suspend fun resolve(url: String): String {
        return when {
            url.contains("disk.yandex.ru") || url.contains("yadi.sk") -> {
                val api = YandexNetwork.createApi()
                api.getDownloadLink(url).href
            }
            else -> url // уже прямой
        }
    }
}
