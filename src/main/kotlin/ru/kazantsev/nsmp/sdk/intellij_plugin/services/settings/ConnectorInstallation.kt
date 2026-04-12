package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

data class ConnectorInstallation(
    val installationId: String,
    val scheme: String,
    val host: String,
    val accessKey: String,
    val ignoreSSL: Boolean,
)
