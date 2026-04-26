package ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.source_roots

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.project.SourceRootMarkerService

@Service(Service.Level.PROJECT)
class SourceRootsVfsChangesListener(
    project: Project
) : BulkFileListener, Disposable {
    private val sourceRootMarkerService = project.service<SourceRootMarkerService>()
    private val busConnection = project.messageBus.connect(this)

    init {
        busConnection.subscribe(VirtualFileManager.VFS_CHANGES, this)
    }

    override fun after(events: List<VFileEvent>) {
        if (events.any { isRelevantEvent(it) }) {
            sourceRootMarkerService.markConfiguredRoots()
        }
    }

    fun isRelevantEvent(event: VFileEvent): Boolean {
        val path = event.path.replace('\\', '/')
        return sourceRootMarkerService.srcRoots.any{path.contains(it)}
    }


    override fun dispose() = busConnection.dispose()
}
