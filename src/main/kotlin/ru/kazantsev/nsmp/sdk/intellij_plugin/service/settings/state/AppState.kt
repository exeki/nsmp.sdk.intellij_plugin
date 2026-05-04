package ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.state

import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.intellij_plugin.Constants

open class AppState {
    var serverPort: Int = Constants.DEFAULT_PORT
    var pathToConfigFile: String = ConnectorParams.getDefaultParamsFilePath()
    var scriptExecuteReadTimeoutMs: Long = Constants.DEFAULT_SCRIPT_EXECUTE_READ_TIMEOUT
}
