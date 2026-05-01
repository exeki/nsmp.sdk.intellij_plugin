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
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcFile


@Service(Service.Level.PROJECT)
class LastSyncedSrcStateService(private val project: Project) {
    private val fileSupportService = project.service<FileSupportService>()
    private val balloonNotificationService = project.service<BalloonNotificationService>()

    companion object {
        private const val NOTIFICATION_TITLE = "NSMP SDK Last sync state update"
        private const val LAST_SYNCED_FOLDER = "last_synced_state"
    }

    fun <T : ISrcFile> updateLastSyncedStateBackground(srcSetRoot: SrcSetRoot<T>) {
        AppExecutorUtil.getAppExecutorService().execute {
            if (project.isDisposed) return@execute
            try {
                WriteCommandAction.runWriteCommandAction(project) {
                    srcSetRoot.values.forEach {
                        updateSrcSetLastSyncedState(it)
                    }
                }
            } catch (error: Throwable) {
                balloonNotificationService.showError(NOTIFICATION_TITLE, error)
            }
        }
    }

    fun getTargetFolderRelativePath(type: SrcType): String {
        return "${Constants.SDK_DIR}/$LAST_SYNCED_FOLDER/${type.code}"
    }

    private fun getTargetFolder(type: SrcType): VirtualFile {
        val path = getTargetFolderRelativePath(type)
        val base = fileSupportService.getProjectVirtualFile()
        return VfsUtil.createDirectoryIfMissing(base, path)
    }

    private fun <T : ISrcFile> updateSrcSetLastSyncedState(
        srcSet: ISrcSet<T>
    ) {
        val folder = getTargetFolder(srcSet.type)
        val format = srcSet.type.format.code
        srcSet.forEach {
            val filename = "${it.code}.$format"
            val file = folder.findChild(filename) ?: folder.createChildData(this, filename)
            VfsUtil.saveText(file, it.file.readText())
        }
    }
}
