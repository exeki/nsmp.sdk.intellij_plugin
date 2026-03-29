package ru.kazantsev.nsdplugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.APP)
@State(name = "NsdPluginSettings", storages = [Storage("nsd-plugin.xml")])
class NsdPluginSettingsService : PersistentStateComponent<NsdPluginSettingsService> {
    var serverPort: Int = 8123
    var uploadUrl: String = "http://localhost:8080/upload"

    init {
        logger.info("NSD Plugin settings service initialized. Default port=$serverPort, uploadUrl=$uploadUrl")
    }

    override fun getState(): NsdPluginSettingsService = this

    override fun loadState(state: NsdPluginSettingsService) {
        XmlSerializerUtil.copyBean(state, this)
        logger.info("NSD Plugin settings loaded. Port=$serverPort, uploadUrl=$uploadUrl")
    }

    companion object {
        fun getInstance(): NsdPluginSettingsService =
            ApplicationManager.getApplication().getService(NsdPluginSettingsService::class.java)

        private val logger = thisLogger()
    }
}
