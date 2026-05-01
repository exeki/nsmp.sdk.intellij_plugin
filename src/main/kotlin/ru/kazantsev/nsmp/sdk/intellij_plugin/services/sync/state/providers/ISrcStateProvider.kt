package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers

import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcVirtualFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet

interface ISrcStateProvider {
    fun getSrcSet(req : SrcSetRequest): SrcSet<SrcVirtualFile>

    fun getSrcSetRoot(req : SrcRequest): SrcSetRoot<SrcVirtualFile>
}