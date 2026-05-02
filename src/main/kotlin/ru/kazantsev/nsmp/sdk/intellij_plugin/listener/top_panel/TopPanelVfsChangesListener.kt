package ru.kazantsev.nsmp.sdk.intellij_plugin.listener.top_panel

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.TopPanelService

@Service(Service.Level.PROJECT)
class TopPanelVfsChangesListener(
    project: Project,
) : BulkFileListener, Disposable {

    private val manager = FileEditorManager.getInstance(project)

    private val busConnection = project.messageBus.connect(this)

    private val topPanelService = project.service<TopPanelService>()

    init {
        busConnection.subscribe(VirtualFileManager.VFS_CHANGES, this)
    }

    override fun after(events: List<VFileEvent>) {
        refreshPanelsForChangedFiles(events)
    }

    private fun refreshPanelsForChangedFiles(events: List<VFileEvent>) {
        val files = events
            .filter(::isPanelRefreshEvent)
            .mapNotNull { it.file }
            .toSet()
        if (files.isEmpty()) return

        files.forEach { file ->
            manager.getEditors(file).forEach { editor ->
                topPanelService.processEditor(editor,  false)
            }
        }
    }

    private fun isPanelRefreshEvent(event: VFileEvent): Boolean {
        return event is VFileMoveEvent || event is VFilePropertyChangeEvent && event.propertyName == VirtualFile.PROP_NAME
    }

    override fun dispose() = busConnection.dispose()

    /*
    private fun affectedUrls(event: VFileEvent): List<String> {
        return when (event) {
            is VFileMoveEvent -> listOf(event.oldParent.url + "/" + event.file.name, event.file.url)
            is VFilePropertyChangeEvent -> {
                val oldName = event.oldValue as? String
                if (event.propertyName == VirtualFile.PROP_NAME && oldName != null) {
                    listOf(event.file.parent.url + "/" + oldName, event.file.url)
                } else listOf(event.file.url)
            }

            else -> event.file?.url?.let(::listOf).orEmpty()
        }
    }
     */

}
