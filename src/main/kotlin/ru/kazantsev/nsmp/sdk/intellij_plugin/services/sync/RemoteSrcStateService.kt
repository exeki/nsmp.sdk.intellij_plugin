package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

@Service(Service.Level.PROJECT)
class RemoteSrcStateService (private val project: Project) {
    private val projectSettingsService = project.service<ProjectSettingsService>()
    private val srcSyncService = projectSettingsService.srcSyncService
    private val fileSupportService = project.service<FileSupportService>()

    companion object {
        //TODO брать откуда то из src sync
        private const val SDK_FOLDER = ".nsmp_sdk"
        //TODO общие константы на проект
        private const val REMOTE_FOLDER = "remote_state"
    }

    fun updateRemoteStateFolder(req: SrcRequest) {
        WriteCommandAction.runWriteCommandAction(project) {
            val data = getRemoteData(req)
            data.entries.forEach {
                updateSrcSetRemoteState(it)
            }
        }
    }

    fun findLastSyncedFile(type: SrcType, code: String): VirtualFile? {
        val folder = getTargetFolder(type)
        val fileName = "$code.${type.format.code}"
        return folder.findChild(fileName)
    }

    fun listRemoteFiles(type: SrcType): Map<String, VirtualFile> {
        val folder = getTargetFolder(type)
        return folder.children
            .asSequence()
            .filter { !it.isDirectory }
            .associateBy { it.nameWithoutExtension }
    }

    private fun getRemoteData(req : SrcRequest): SrcSetRoot<RemoteTextInfo> {
        return srcSyncService.remoteSrcService.getRemoteSrc(req)
    }

    private fun getTargetFolder(type: SrcType): VirtualFile {
        val path = "$SDK_FOLDER/$REMOTE_FOLDER/${type.code}"
        val base = fileSupportService.getProjectVirtualFile()
        return VfsUtil.createDirectoryIfMissing(base, path)
    }

    private fun updateSrcSetRemoteState(srcSet: ISrcSet<RemoteTextInfo>) {
        val folder = getTargetFolder(srcSet.type)
        val format = srcSet.type.format.code
        srcSet.forEach {
            val filename = "${it.code}.$format"
            val file = folder.findChild(filename) ?: folder.createChildData(this, filename)
            VfsUtil.saveText(file, it.text)
        }
    }
}
