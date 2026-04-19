package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.state

import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams

open class AppState {
    var serverPort: Int = 8123
    var pathToConfigFile: String = ConnectorParams.getDefaultParamsFilePath()
}