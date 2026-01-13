package com.kirya.everlastingmods.data.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(
    val updatedAt: String? = null,
    val mods: List<ModDto> = emptyList()
)

@Serializable
data class ModDto(
    val id: String,
    val title: String,
    val author: String,
    val version: String,
    val gameVersion: String? = null,
    val downloadUrl: String,
    val sha256: String? = null,
    val sizeBytes: Long? = null,
    val previewUrl: String? = null,
    val heroines: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)
