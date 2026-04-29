package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import java.nio.file.Path
import java.time.Instant

data class DiffRow(
    val type: SrcType,
    val code: String,
    val leftPath: Path?,
    val rightPath: Path?,
    val leftSet: CompareSetType,
    val rightSet: CompareSetType,
    val status: DiffStatus,
    val lastSyncDate: Instant?
)