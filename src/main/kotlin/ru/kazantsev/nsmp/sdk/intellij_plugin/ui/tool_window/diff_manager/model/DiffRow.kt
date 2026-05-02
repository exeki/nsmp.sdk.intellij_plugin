package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model

import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.DiffStatus
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType

data class DiffRow(
    val type: SrcType,
    val code: String,
    val leftFile : VirtualFile?,
    val leftSet: SrcState,
    val rightFile : VirtualFile?,
    val rightSet: SrcState,
    val status: DiffStatus
)