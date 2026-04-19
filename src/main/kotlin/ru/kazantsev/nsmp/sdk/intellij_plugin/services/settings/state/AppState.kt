package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.state

import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams

open class AppState {
    var serverPort: Int = 8123
    var pathToConfigFile: String = ConnectorParams.getDefaultParamsFilePath()

    fun translateValues(otherState: AppState? = null) : AppState {
        val state = otherState ?: AppState()
        state.serverPort = serverPort
        state.pathToConfigFile = pathToConfigFile
        return state
    }
}