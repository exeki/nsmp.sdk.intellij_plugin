package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.SrcSyncUnsupportedType
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.UnknownSourceType
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.LastSyncedSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
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
    companion object {
        private val logger = thisLogger()
    }

    val projectSettingsService = project.service<ProjectSettingsService>()

    val lastSyncedSrcStateService = project.service<LastSyncedSrcStateService>()

    val fileSupportService = project.service<FileSupportService>()

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
            ?: throw SrcSyncUnsupportedType(MessageBundle.message("sync.error.file.type.unknown"))
    }

    fun getSrcRequestForFile(file: VirtualFile, type: SrcType): SrcRequest? {
        return when (type) {
            SrcType.SCRIPT -> SrcRequest(scripts = setOf(resolveSrcCode(file)))
            SrcType.MODULE -> SrcRequest(modules = setOf(resolveSrcCode(file)))
            SrcType.ADV_IMPORT -> SrcRequest(advImports = setOf(resolveSrcCode(file)))
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
        else null
    }

    fun isSupportedFile(file: VirtualFile): Boolean {
        return getSrcType(file) != null || file.extension.equals("groovy", ignoreCase = true)
    }

    fun pull(request: SrcRequest): SrcSetRoot<SrcFileChecksum> {
        val result = createSrcSyncService().pull(request)
        lastSyncedSrcStateService.updateLastSyncedStateBackground(result)
        return result
    }

    fun pull(
        file: VirtualFile,
        onSuccessCallback: (SrcSetRoot<SrcFileChecksum>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val request = getSrcRequestForFileElseThrow(file)
        pull(request, onSuccessCallback, onFailureCallback)
    }

    fun pull(
        request: SrcRequest,
        onSuccessCallback: (SrcSetRoot<SrcFileChecksum>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.pull.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val result = pull(request)
                    ApplicationManager.getApplication().invokeLater { onSuccessCallback(result) }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater { onFailureCallback(error) }
                } finally {
                    fileSupportService.refreshProjectExplorer()
                }
            }
        })
    }


    fun syncCheck(request: SrcRequest): SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>> {
        return createSrcSyncService().syncCheck(request)
    }

    fun syncCheck(
        file: VirtualFile,
        onSuccessCallback: (SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val request = getSrcRequestForFileElseThrow(file)
        syncCheck(request, onSuccessCallback, onFailureCallback)
    }

    fun syncCheck(
        request: SrcRequest,
        onSuccessCallback: (SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.sync.check.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val result = syncCheck(request)
                    ApplicationManager.getApplication().invokeLater { onSuccessCallback(result) }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater { onFailureCallback(error) }
                } finally {
                    fileSupportService.refreshProjectExplorer()
                }
            }
        })
    }

    fun push(request: SrcRequest, force: Boolean): SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>> {
        val result = createSrcSyncService().push(request, force)
        lastSyncedSrcStateService.updateLastSyncedStateBackground(result.convert { _, pair -> pair.left })
        return result
    }

    fun push(
        file: VirtualFile,
        force: Boolean,
        onSuccessCallback: (SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val request = getSrcRequestForFileElseThrow(file)
        push(request, force, onSuccessCallback, onFailureCallback)
    }

    fun push(
        request: SrcRequest,
        force: Boolean,
        onSuccessCallback: (SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.push.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val result = push(request, force)
                    ApplicationManager.getApplication().invokeLater { onSuccessCallback(result) }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater { onFailureCallback(error) }
                } finally {
                    fileSupportService.refreshProjectExplorer()
                }
            }
        })
    }
}
