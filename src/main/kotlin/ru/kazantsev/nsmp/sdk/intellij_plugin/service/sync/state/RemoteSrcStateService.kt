package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcTextChecksum


@Service(Service.Level.PROJECT)
class RemoteSrcStateService(private val project: Project) {
    private val syncService = project.service<SyncService>()
    private val fileSupportService = project.service<FileSupportService>()

    companion object {
        private const val REMOTE_FOLDER = "remote_state"
    }

    fun updateRemoteState(req: SrcRequest) {
        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                val remoteReq = syncService.createSrcSyncService()
                    .localSrcService
                    .getLocalSrcSetRoot(req)
                    .convertToRequest()
                getRemoteData(remoteReq).forEach { updateSrcSetRemoteState(it.value) }
            }
        }
    }

    fun getTargetFolderRelativePath(type: SrcType): String {
        return "${Constants.SDK_DIR}/$REMOTE_FOLDER/${type.code}"
    }

    private fun getRemoteData(req: SrcRequest): SrcSetRoot<SrcTextChecksum> {
        return syncService.createSrcSyncService().remoteSrcService.getRemoteSrc(req)
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
