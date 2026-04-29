package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo

@Service(Service.Level.PROJECT)
class LastSyncedStateService(private val project: Project) {
    private val fileSupportService = project.service<FileSupportService>()
    private val balloonNotificationService = project.service<BalloonNotificationService>()

    companion object {
        private const val NOTIFICATION_TITLE = "NSMP SDK Last Sync State Update"
        //TODO брать откуда то из src sync
        private const val SDK_FOLDER = ".nsmp_sdk"
        //TODO общие константы на проект
        private const val LAST_SYNCED_FOLDER = "last_synced_state"
    }

    fun updateLastSyncedState(srcSetRoot: SrcSetRoot<LocalFileInfo>) {
        WriteCommandAction.runWriteCommandAction(project) {
            srcSetRoot.entries.forEach {
                updateSrcSetLastSyncedState(it)
            }
        }
    }

    fun updateLastSyncedStateBackground(srcSetRoot: SrcSetRoot<LocalFileInfo>) {
        AppExecutorUtil.getAppExecutorService().execute {
            if (project.isDisposed) return@execute
            try {
                updateLastSyncedState(srcSetRoot)
            } catch (error: Throwable) {
                balloonNotificationService.showError(NOTIFICATION_TITLE, error)
            }
        }
    }

    fun findLastSyncedFile(type: SrcType, code: String): VirtualFile? {
        val folder = getTargetFolder(type)
        val fileName = "$code.${type.format.code}"
        return folder.findChild(fileName)
    }

    fun listLastSyncedFiles(type: SrcType): Map<String, VirtualFile> {
        val folder = getTargetFolder(type)
        return folder.children
            .asSequence()
            .filter { !it.isDirectory }
            .associateBy { it.nameWithoutExtension }
    }

    private fun getTargetFolder(type: SrcType): VirtualFile {
        val path = "$SDK_FOLDER/$LAST_SYNCED_FOLDER/${type.code}"
        val base = fileSupportService.getProjectVirtualFile()
        return VfsUtil.createDirectoryIfMissing(base, path)
    }

    private fun updateSrcSetLastSyncedState(
        srcSet: ISrcSet<LocalFileInfo>
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
