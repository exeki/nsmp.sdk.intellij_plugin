package ru.kazantsev.nsmp.sdk.intellij_plugin.unused_concepts.source_roots

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

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
        sourceRootMarkerService.markConfiguredRoots()
    }

    override fun dispose() = busConnection.dispose()
}
