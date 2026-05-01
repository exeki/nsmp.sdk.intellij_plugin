package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcDiffPair
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcVirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers.CurrentSrcStateProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers.ISrcStateProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers.LastSyncSrcStateProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.providers.RemoteSrcStateProvider
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet

@Service(Service.Level.PROJECT)
class DiffService(
    private val project: Project
) {

    fun getDiffPairs(req: SrcRequest, left: SrcState, right: SrcState): SrcSetRoot<SrcDiffPair> {
        val leftRoot = loadStateSrcSetRoot(left, req)
        val rightRoot = loadStateSrcSetRoot(right, req)
        return SrcSetRoot.byEnumIterator { type ->
            val keys = mutableSetOf<String>()
            val left = leftRoot[type]
            val right = rightRoot[type]
            keys.addAll(left.map.keys)
            keys.addAll(right.map.keys)
            keys.map {
                SrcDiffPair(
                    left = left.getByCode(it),
                    right = right.getByCode(it),
                    code = it
                )
            }.toSet()
        }
    }

    private fun getProviderByState(state: SrcState): ISrcStateProvider {
        return when (state) {
            SrcState.CURRENT -> CurrentSrcStateProvider(project)
            SrcState.LAST_SYNC -> LastSyncSrcStateProvider(project)
            SrcState.REMOTE -> RemoteSrcStateProvider(project)
        }
    }

    private fun loadStateSrcSetRoot(state: SrcState, request: SrcRequest): SrcSetRoot<SrcVirtualFile> {
        return getProviderByState(state).getSrcSetRoot(request)
    }

    @Suppress("unused")
    private fun loadStateSrcSet(state: SrcState, request: SrcSetRequest): SrcSet<SrcVirtualFile> {
        return getProviderByState(state).getSrcSet(request)
    }
}