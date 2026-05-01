@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcVirtualFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.service.LocalSrcService


class CurrentSrcStateProvider(
    val project: Project,
) : ISrcStateProvider {

    private val localSrcService: LocalSrcService = project.service<SyncService>()
        .createSrcSyncService()
        .localSrcService

    fun convert(lfs: LocalFileSystem, src: SrcFileChecksum): SrcVirtualFile {
        val vf = lfs.findFileByIoFile(src.file)
        if (vf == null) throw RuntimeException("Can't find virtual file ${src.file.toPath()}")
        return SrcVirtualFile(
            virtualFile = vf,
            code = src.code,
        )
    }

    override fun getSrcSet(req: SrcSetRequest): SrcSet<SrcVirtualFile> {
        val lfs = LocalFileSystem.getInstance()
        return localSrcService.getLocalSrcSet(req).convert {
            convert(lfs, it)
        }
    }

    override fun getSrcSetRoot(req: SrcRequest): SrcSetRoot<SrcVirtualFile> {
        val lfs = LocalFileSystem.getInstance()
        return localSrcService.getLocalSrcSetRoot(req).convert { _, src ->
            convert(lfs, src)
        }
    }
}