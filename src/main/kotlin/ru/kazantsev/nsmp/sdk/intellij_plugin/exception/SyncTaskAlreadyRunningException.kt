package ru.kazantsev.nsmp.sdk.intellij_plugin.exception

class SyncTaskAlreadyRunningException(
    override val message: String = "Another sync task is already running"
) : LogicException(message)
