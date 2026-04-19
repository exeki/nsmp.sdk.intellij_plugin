package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.state.AppState

@Service(Service.Level.APP)
@State(name = "NsdPluginSettings", storages = [Storage("nsd-plugin.xml")])
class AppSettingsService : AppState(), PersistentStateComponent<AppState> {

    init {
        logger.info("NSD Plugin settings service initialized. Default port=$serverPort")
    }

    var settings = AppState()

    override fun getState(): AppState {
        return this.settings
    }

    override fun loadState(state: AppState) {
       this.settings = state
    }

    companion object {
        fun getInstance(): AppSettingsService =
            ApplicationManager.getApplication().getService(AppSettingsService::class.java)

        private val logger = thisLogger()
    }
}
