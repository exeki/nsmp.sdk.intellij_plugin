package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.RemoteSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcVirtualFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet

class RemoteSrcStateProvider(
    val project: Project
) : ISrcStateProvider {

    private val remoteSrcStateService: RemoteSrcStateService = project.service<RemoteSrcStateService>()
    private val fileSupportService: FileSupportService = project.service<FileSupportService>()

    override fun getSrcSet(req: SrcSetRequest): SrcSet<SrcVirtualFile> {
        val path = remoteSrcStateService.getTargetFolderRelativePath(req.type)
        val folder = fileSupportService.getVirtualFileByRelativePath(path)
        return if (folder == null || !folder.exists()) SrcSet.empty(req.type)
        else {
            val set = folder.children
                .filter {
                    val code = it.nameWithoutExtension
                    !req.excludedCodes.contains(code) && (req.all || req.includedCodes.contains(code))
                }
                .map {
                    SrcVirtualFile(
                        code = it.nameWithoutExtension,
                        virtualFile = it
                    )
                }.toSet()
            SrcSet(set, req.type)
        }
    }

    override fun getSrcSetRoot(req: SrcRequest): SrcSetRoot<SrcVirtualFile> {
        return SrcSetRoot.byEnumIterator { type ->
            getSrcSet(req.getSetRequest(type))
        }
    }
}