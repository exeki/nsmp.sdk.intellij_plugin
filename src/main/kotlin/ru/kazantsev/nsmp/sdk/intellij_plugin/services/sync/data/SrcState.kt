package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data

enum class SrcState(val code: String) {
    CURRENT("current"),
    REMOTE("remote"),
    LAST_SYNC("last_sync");
}