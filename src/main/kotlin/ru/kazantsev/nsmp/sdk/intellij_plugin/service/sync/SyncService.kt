package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.UnknownSourceType
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSyncCheckPair
import java.nio.file.Path
import java.nio.file.Paths

@Service(Service.Level.PROJECT)
class SyncService(private val project: Project) {


    val projectSettingsService = project.service<ProjectSettingsService>()

    fun createSrcSyncService(): SrcSyncService {
        return SrcSyncService(
            projectSettingsService.connectorParams,
            projectSettingsService.srcFoldersParams
        )
    }

    fun resolveSrcCode(file: VirtualFile): String {
        return file.nameWithoutExtension.ifBlank { file.name }
    }

    fun getSrcRequestForFileElseThrow(file: VirtualFile): SrcRequest {
        val type = getSrcTypeElseThrow(file)
        return getSrcRequestForFile(file, type)
    }

    fun getSrcRequestForFile(file: VirtualFile, type: SrcType): SrcRequest {
        val code = resolveSrcCode(file)
        return getSrcRequestForCode(code, type)
    }

    fun getSrcRequestForCode(code: String, type: SrcType): SrcRequest {
        return when (type) {
            SrcType.SCRIPT -> SrcRequest(scripts = setOf(code))
            SrcType.MODULE -> SrcRequest(modules = setOf(code))
            SrcType.ADV_IMPORT -> SrcRequest(advImports = setOf(code))
        }
    }

    fun getSrcTypeElseThrow(file: VirtualFile): SrcType {
        return getSrcType(file) ?: throw UnknownSourceType()
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
        else null
    }

    fun isSupportedFile(file: VirtualFile): Boolean {
        return getSrcType(file) != null || file.extension.equals("groovy", ignoreCase = true)
    }

    fun pull(request: SrcRequest): SrcSetRoot<SrcFileChecksum> {
        return createSrcSyncService().pull(request)
    }

    fun syncCheck(request: SrcRequest): SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>> {
        return createSrcSyncService().syncCheck(request)
    }

    fun push(request: SrcRequest, force: Boolean): SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>> {
        return createSrcSyncService().push(request, force)
    }

}
