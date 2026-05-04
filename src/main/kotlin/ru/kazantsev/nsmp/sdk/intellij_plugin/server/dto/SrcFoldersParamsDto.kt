package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams

@Serializable
data class SrcFoldersParamsDto(
    val projectAbsolutePath: String,
    val scriptsRelativePath: String,
    val modulesRelativePath: String,
    val advImportsRelativePath: String,
) {
    companion object {
        fun from(params: SrcFoldersParams): SrcFoldersParamsDto =
            SrcFoldersParamsDto(
                projectAbsolutePath = params.getProjectAbsolutePathString(),
                scriptsRelativePath = params.getScriptsRelativePathString(),
                modulesRelativePath = params.getModulesRelativePathString(),
                advImportsRelativePath = params.getAdvImportsRelativePathString(),
            )
    }
}
