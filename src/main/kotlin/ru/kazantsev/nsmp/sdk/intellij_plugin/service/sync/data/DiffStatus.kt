package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data

enum class DiffStatus(val code: String)  {
    ADDED("added"),
    MODIFIED("modified"),
    DELETED("deleted"),
    UNCHANGED("same");
}
