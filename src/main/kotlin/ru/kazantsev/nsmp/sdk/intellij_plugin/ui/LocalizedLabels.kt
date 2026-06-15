package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.DiffStatus
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter

fun SrcState.localizedName(): String = when (this) {
    SrcState.CURRENT -> MessageBundle.message("sync.state.current")
    SrcState.REMOTE -> MessageBundle.message("sync.state.remote")
    SrcState.LAST_SYNC -> MessageBundle.message("sync.state.last.sync")
}

fun DiffStatus.localizedName(): String = when (this) {
    DiffStatus.ADDED -> MessageBundle.message("sync.diff.status.added")
    DiffStatus.MODIFIED -> MessageBundle.message("sync.diff.status.modified")
    DiffStatus.DELETED -> MessageBundle.message("sync.diff.status.deleted")
    DiffStatus.UNCHANGED -> MessageBundle.message("sync.diff.status.unchanged")
}

fun DiffTypeFilter.localizedName(): String = when (this) {
    DiffTypeFilter.ALL -> MessageBundle.message("sync.diff.type.all")
    DiffTypeFilter.SCRIPT -> MessageBundle.message("sync.diff.type.script")
    DiffTypeFilter.MODULE -> MessageBundle.message("sync.diff.type.module")
    DiffTypeFilter.ADV_IMPORT -> MessageBundle.message("sync.diff.type.adv.import")
}
