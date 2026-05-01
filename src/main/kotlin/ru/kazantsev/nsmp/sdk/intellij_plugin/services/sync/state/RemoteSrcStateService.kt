package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcTextChecksum


@Service(Service.Level.PROJECT)
class RemoteSrcStateService(private val project: Project) {
    private val projectSettingsService = project.service<ProjectSettingsService>()
    private val srcSyncService = projectSettingsService.srcSyncService
    private val fileSupportService = project.service<FileSupportService>()
    private val balloonNotificationService = project.service<BalloonNotificationService>()

    companion object {
        private const val REMOTE_FOLDER = "remote_state"
        private const val NOTIFICATION_TITLE = "NSMP SDK Remote state update"
    }

    fun updateRemoteStateBackground(req: SrcRequest) {
        AppExecutorUtil.getAppExecutorService().execute {
            if (project.isDisposed) return@execute
            try {
                WriteCommandAction.runWriteCommandAction(project) {
                    val remoteReq = srcSyncService.localSrcService.getLocalSrcSetRoot(req).convertToRequest()
                    val data = getRemoteData(remoteReq)
                    data.forEach { (_, src) ->
                        updateSrcSetRemoteState(src)
                    }
                }
            } catch (error: Throwable) {
                balloonNotificationService.showError(NOTIFICATION_TITLE, error)
            }
        }
    }

    fun getTargetFolderRelativePath(type: SrcType): String {
        return "${Constants.SDK_DIR}/$REMOTE_FOLDER/${type.code}"
    }

    private fun getRemoteData(req: SrcRequest): SrcSetRoot<SrcTextChecksum> {
        return srcSyncService.remoteSrcService.getRemoteSrc(req)
    }

    private fun getOrCreateTargetFolder(type: SrcType): VirtualFile {
        val path = getTargetFolderRelativePath(type)
        val base = fileSupportService.getProjectVirtualFile()
        return VfsUtil.createDirectoryIfMissing(base, path)
    }

    private fun updateSrcSetRemoteState(srcSet: ISrcSet<SrcTextChecksum>) {
        val folder = getOrCreateTargetFolder(srcSet.type)
        val format = srcSet.type.format.code
        srcSet.forEach {
            val filename = "${it.code}.$format"
            val file = folder.findChild(filename) ?: folder.createChildData(this, filename)
            VfsUtil.saveText(file, it.text)
        }
    }
}
