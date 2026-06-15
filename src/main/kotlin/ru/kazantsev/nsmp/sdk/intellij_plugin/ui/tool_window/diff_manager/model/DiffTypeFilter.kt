package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType

enum class DiffTypeFilter(val code: String, val srcType : SrcType) {
    ALL("all", SrcType.SCRIPT),
    SCRIPT("scripts", SrcType.SCRIPT),
    MODULE("module", SrcType.MODULE),
    ADV_IMPORT("advImport", SrcType.ADV_IMPORT);
}