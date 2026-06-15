package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType

enum class DiffTypeFilter(
    val code: String,
    val srcType: SrcType,
    private val messageKey: String,
) {
    ALL("all", SrcType.SCRIPT, "sync.diff.type.all"),
    SCRIPT("scripts", SrcType.SCRIPT, "sync.diff.type.script"),
    MODULE("module", SrcType.MODULE, "sync.diff.type.module"),
    ADV_IMPORT("advImport", SrcType.ADV_IMPORT, "sync.diff.type.adv.import");

    fun localizedName(): String = MessageBundle.message(messageKey)
}
