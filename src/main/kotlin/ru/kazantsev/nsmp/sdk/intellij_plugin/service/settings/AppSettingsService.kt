package ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.state.AppState

@Service(Service.Level.APP)
@State(name = "NsdPluginSettings", storages = [Storage("nsd-plugin.xml")])
class AppSettingsService : PersistentStateComponent<AppState> {

    var settings = AppState()

    override fun getState(): AppState {
        return this.settings
    }

    override fun loadState(state: AppState) {
        this.settings = state
    }

    val scriptExecuteReadTimeout: Long
        get() = state.scriptExecuteReadTimeoutMs

    val pathToConfigFile: String
        get() = state.pathToConfigFile

    val serverPort: Int
        get() = state.serverPort

    companion object {
        fun getInstance(): AppSettingsService =
            ApplicationManager.getApplication().getService(AppSettingsService::class.java)
    }
}
