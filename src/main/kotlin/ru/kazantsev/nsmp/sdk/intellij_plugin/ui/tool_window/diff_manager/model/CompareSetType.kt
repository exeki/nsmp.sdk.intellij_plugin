package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model

enum class CompareSetType(val uiCode: String) {
    LOCAL("local"),
    REMOTE("remote"),
    LAST_SYNC("last sync");

    override fun toString(): String = uiCode
}