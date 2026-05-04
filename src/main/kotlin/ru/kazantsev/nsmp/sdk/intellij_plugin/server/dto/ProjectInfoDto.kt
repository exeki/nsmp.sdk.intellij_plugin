package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectInfoDto(
    val name: String,
    val basePath: String? = null,
    val isDisposed: Boolean,
    val isInitialized: Boolean,
    val nsmpSdk : ProjectPluginInfoDto?
)
