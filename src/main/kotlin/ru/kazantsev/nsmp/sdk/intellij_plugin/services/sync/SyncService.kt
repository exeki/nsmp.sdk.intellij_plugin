package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.SrcSyncUnsupportedType
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.UnknownSourceType
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import java.nio.file.Path
import java.nio.file.Paths

@Service(Service.Level.PROJECT)
class SyncService(private val project: Project) {

    val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    private fun createSrcService(): SrcSyncService {
        return SrcSyncService(
            projectSettingsService.connectorParams,
            ObjectMapper(),
            projectSettingsService.srcFoldersParams
        )
    }

    private fun resolveSrcCode(file: VirtualFile): String {
        return file.nameWithoutExtension.ifBlank { file.name }
    }

    fun getSrcRequestForFileElseThrow(file: VirtualFile): SrcRequest {
        val type = getSrcTypeElseThrow(file)
        return getSrcRequestForFile(file, type)
            ?: throw SrcSyncUnsupportedType(MessageBundle.message("sync.error.file.type.unknown"))
    }

    fun getSrcRequestForFile(file: VirtualFile, type: SrcType): SrcRequest? {
        return when (type) {
            SrcType.SCRIPT -> SrcRequest(scripts = listOf(resolveSrcCode(file)))
            SrcType.MODULE -> SrcRequest(modules = listOf(resolveSrcCode(file)))
            SrcType.ADV_IMPORT -> SrcRequest(advImports = listOf(resolveSrcCode(file)))
            else -> null
        }
    }

    fun getSrcTypeElseThrow(file: VirtualFile): SrcType {
        return getSrcType(file) ?: throw UnknownSourceType("Unknown source type")
    }

    fun getSrcType(file: VirtualFile): SrcType? {
        val projectPathString = project.basePath ?: throw RuntimeException("project.basePath is null")
        val projectPath = Path.of(projectPathString)
        val filePath = Paths.get(file.path).normalize().toAbsolutePath()

        val srcFoldersParams = projectSettingsService.srcFoldersParams
        val s = srcFoldersParams.getScriptsRelativePathString()
        val m = srcFoldersParams.getModulesRelativePathString()
        val a = srcFoldersParams.getAdvImportsRelativePathString()

        return if (filePath.startsWith(projectPath.resolve(s).normalize())) SrcType.SCRIPT
        else if (filePath.startsWith(projectPath.resolve(m).normalize())) SrcType.MODULE
        else if (filePath.startsWith(projectPath.resolve(a).normalize())) SrcType.ADV_IMPORT
        else if (file.extension.equals("groovy", ignoreCase = true)) SrcType.GROOVY
        else null
    }

    fun isSupportedFile(file: VirtualFile): Boolean {
        return getSrcType(file) != null
    }

    fun pull(request: SrcRequest): SrcDtoRoot {
        return createSrcService().pull(request)
    }

    fun syncCheck(request: SrcRequest): SrcInfoRoot {
        return createSrcService().syncCheck(request)
    }

    fun push(request: SrcRequest, force: Boolean): SrcInfoRoot {
        return createSrcService().push(request, force)
    }

}