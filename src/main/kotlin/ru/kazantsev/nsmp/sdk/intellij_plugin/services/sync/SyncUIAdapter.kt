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
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest

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

    fun saveVirtualFile(virtualFile: VirtualFile) {
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        if (document != null) FileDocumentManager.getInstance().saveDocument(document)
        else throw RuntimeException("Can't save virtual file ${virtualFile.name}")
    }

    fun pull(
        file: VirtualFile,
        onSuccessCallback: (SrcDtoRoot) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        saveVirtualFile(file)
        val request = service.getSrcRequestForFileElseThrow(file)
        pull(request, onSuccessCallback, onFailureCallback)
    }

    fun pull(
        request: SrcRequest,
        onSuccessCallback: (SrcDtoRoot) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.pull.title")
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                runCatching {
                    service.pull(request)
                }.onSuccess { result ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer()
                        onSuccessCallback(result)
                    }
                }.onFailure { error ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer()
                        onFailureCallback(error)
                    }
                }
            }
        })
    }

    fun push(
        file: VirtualFile,
        force: Boolean,
        onSuccessCallback: (SrcInfoRoot) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        saveVirtualFile(file)
        val request = service.getSrcRequestForFileElseThrow(file)
        push(request, force, onSuccessCallback, onFailureCallback)
    }

    fun push(
        request: SrcRequest,
        force: Boolean,
        onSuccessCallback: (SrcInfoRoot) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.push.title")
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                runCatching {
                    service.push(request, force)
                }.onSuccess { result ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer()
                        onSuccessCallback(result)
                    }
                }.onFailure { error ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer()
                        onFailureCallback(error)
                    }
                }
            }
        })
    }

    fun syncCheck(
        file: VirtualFile,
        onSuccessCallback: (SrcInfoRoot) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        saveVirtualFile(file)
        val request = service.getSrcRequestForFileElseThrow(file)
        syncCheck(request, onSuccessCallback, onFailureCallback)
    }

    fun syncCheck(
        request: SrcRequest,
        onSuccessCallback: (SrcInfoRoot) -> Unit,
        onFailureCallback: (Throwable) -> Unit
    ) {
        val backgroundTaskTitle = MessageBundle.message("sync.command.sync.check.title")
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                runCatching {
                    service.syncCheck(request)
                }.onSuccess { result ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer()
                        onSuccessCallback(result)
                    }
                }.onFailure { error ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer()
                        onFailureCallback(error)
                    }
                }
            }
        })
    }
}
