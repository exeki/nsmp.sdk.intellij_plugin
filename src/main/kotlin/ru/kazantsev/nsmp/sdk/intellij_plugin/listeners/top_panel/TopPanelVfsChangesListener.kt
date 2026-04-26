package ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.top_panel

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.TopPanelService

@Service(Service.Level.PROJECT)
class TopPanelVfsChangesListener(
    private val project: Project,
) : BulkFileListener, Disposable {

    val busConnection = project.messageBus.connect(this)

    init {
        busConnection.subscribe(VirtualFileManager.VFS_CHANGES, this)
    }

    private val syncService = project.service<SyncService>()

    private val topPanelService = project.service<TopPanelService>()

    override fun after(events: List<VFileEvent>) {
        refreshPanelsForChangedFiles(events)
    }

    private fun refreshPanelsForChangedFiles(events: List<VFileEvent>) {
        val affectedUrls = events
            .filter(::isPanelRefreshEvent)
            .flatMap(::affectedUrls)
            .toSet()
        if (affectedUrls.isEmpty()) return

        val manager = FileEditorManager.getInstance(project)
        manager.allEditors.forEach { editor ->
            val fileUrl = editor.getUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY) ?: return@forEach
            if (fileUrl !in affectedUrls) return@forEach

            val file = editor.file ?: return@forEach
            if (!syncService.isSupportedFile(file)) {
                topPanelService.removeTopPanel(manager, editor)
                return@forEach
            }
            topPanelService.installTopPanel(manager, editor, file, true)
        }
    }

    private fun isPanelRefreshEvent(event: VFileEvent): Boolean {
        return event is VFileContentChangeEvent ||
            event is VFileMoveEvent ||
            event is VFilePropertyChangeEvent && event.propertyName == VirtualFile.PROP_NAME
    }

    private fun affectedUrls(event: VFileEvent): List<String> {
        return when (event) {
            is VFileMoveEvent -> listOf(event.oldParent.url + "/" + event.file.name, event.file.url)
            is VFilePropertyChangeEvent -> {
                val oldName = event.oldValue as? String
                if (event.propertyName == VirtualFile.PROP_NAME && oldName != null) {
                    listOf(event.file.parent.url + "/" + oldName, event.file.url)
                } else {
                    listOf(event.file.url)
                }
            }
            else -> event.file?.url?.let(::listOf).orEmpty()
        }
    }

    override fun dispose() = busConnection.dispose()

}
