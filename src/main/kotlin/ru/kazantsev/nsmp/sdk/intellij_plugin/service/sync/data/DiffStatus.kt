package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

enum class DiffStatus(
    val code: String,
    private val messageKey: String,
) {
    ADDED("added", "sync.diff.status.added"),
    MODIFIED("modified", "sync.diff.status.modified"),
    DELETED("deleted", "sync.diff.status.deleted"),
    UNCHANGED("same", "sync.diff.status.unchanged");

    fun localizedName(): String = MessageBundle.message(messageKey)
}
