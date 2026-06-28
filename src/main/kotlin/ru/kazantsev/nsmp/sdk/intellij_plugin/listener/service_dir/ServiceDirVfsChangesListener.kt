package ru.kazantsev.nsmp.sdk.intellij_plugin.listener.service_dir

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ServiceDirSetupService
import ru.kazantsev.nsmp.sdk.sources_sync.Constants

@Service(Service.Level.PROJECT)
class ServiceDirVfsChangesListener(
    private val project: Project
) : BulkFileListener, Disposable {

    private val busConnection = project.messageBus.connect(this)
    private val serviceDirSetupService = project.service<ServiceDirSetupService>()

    init {
        busConnection.subscribe(VirtualFileManager.VFS_CHANGES, this)
    }

    override fun after(events: List<VFileEvent>) {
        if (project.isDisposed) return
        if (events.any(::isServiceDirCreated)) {
            serviceDirSetupService.ensureServiceDirLayout()
        }
    }

    private fun isServiceDirCreated(event: VFileEvent): Boolean {
        if (event !is VFileCreateEvent || !event.isDirectory) return false
        val path = event.path.replace('\\', '/')
        val serviceDir = "/${Constants.SDK_DIR}"
        return path.endsWith(serviceDir)
    }

    override fun dispose() = busConnection.dispose()
}
