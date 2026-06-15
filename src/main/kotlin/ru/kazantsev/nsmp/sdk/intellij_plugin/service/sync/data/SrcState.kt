package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

enum class SrcState(
    val code: String,
    private val messageKey: String,
) {
    CURRENT("current", "sync.state.current"),
    REMOTE("remote", "sync.state.remote"),
    LAST_SYNC("last_sync", "sync.state.last.sync");

    fun localizedName(): String = MessageBundle.message(messageKey)
}
