package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

@Service(Service.Level.PROJECT)
class SyncUIAdapter(private val project: Project) {

    private val service = project.service<SyncService>()

    private fun refreshProjectExplorer() {
        VirtualFileManager.getInstance().asyncRefresh {
            ApplicationManager.getApplication().invokeLater {
                ProjectView.getInstance(project).refresh()
            }
        }
    }

    fun isSupportedFile(file: VirtualFile): Boolean {
        return service.isSupportedFile(file)
    }

    fun getSrcType(file: VirtualFile): SrcType? {
        return service.getSrcType(file)
    }

    fun pull(
        file: VirtualFile,
        onSuccessCallback: (SrcSetRoot<LocalFileInfo>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val request = service.getSrcRequestForFileElseThrow(file)
        pull(request, onSuccessCallback, onFailureCallback)
    }

    fun pull(
        request: SrcRequest,
        onSuccessCallback: (SrcSetRoot<LocalFileInfo>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.pull.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val result = service.pull(request)
                    ApplicationManager.getApplication().invokeLater { onSuccessCallback(result) }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater { onFailureCallback(error) }
                } finally {
                    refreshProjectExplorer()
                }
            }
        })
    }

    fun push(
        file: VirtualFile,
        force: Boolean,
        onSuccessCallback: (SrcSetRoot<SrcPair<LocalFileInfo, RemoteInfo>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val request = service.getSrcRequestForFileElseThrow(file)
        push(request, force, onSuccessCallback, onFailureCallback)
    }

    fun push(
        request: SrcRequest,
        force: Boolean,
        onSuccessCallback: (SrcSetRoot<SrcPair<LocalFileInfo, RemoteInfo>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.push.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val result = service.push(request, force)
                    ApplicationManager.getApplication().invokeLater { onSuccessCallback(result) }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater { onFailureCallback(error) }
                } finally {
                    refreshProjectExplorer()
                }
            }
        })
    }

    fun syncCheck(
        file: VirtualFile,
        onSuccessCallback: (SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val request = service.getSrcRequestForFileElseThrow(file)
        syncCheck(request, onSuccessCallback, onFailureCallback)
    }

    fun syncCheck(
        request: SrcRequest,
        onSuccessCallback: (SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>>) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.sync.check.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val result = service.syncCheck(request)
                    ApplicationManager.getApplication().invokeLater { onSuccessCallback(result) }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater { onFailureCallback(error) }
                } finally {
                    refreshProjectExplorer()
                }
            }
        })
    }
}
