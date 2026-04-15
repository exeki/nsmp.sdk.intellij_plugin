package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.xmlb.XmlSerializerUtil
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams

@Service(Service.Level.APP)
@State(name = "NsdPluginSettings", storages = [Storage("nsd-plugin.xml")])
class AppSettingsService : PersistentStateComponent<AppSettingsService> {
    var serverPort: Int = 8123
    var connectorConfigPath: String = ConnectorParams.getDefaultParamsFilePath()

    init {
        logger.info("NSD Plugin settings service initialized. Default port=$serverPort")
    }

    override fun getState(): AppSettingsService = this

    override fun loadState(state: AppSettingsService) {
        XmlSerializerUtil.copyBean(state, this)
        logger.info("NSD Plugin settings loaded. Port=$serverPort")
    }

    companion object {
        fun getInstance(): AppSettingsService =
            ApplicationManager.getApplication().getService(AppSettingsService::class.java)

        private val logger = thisLogger()
    }
}
