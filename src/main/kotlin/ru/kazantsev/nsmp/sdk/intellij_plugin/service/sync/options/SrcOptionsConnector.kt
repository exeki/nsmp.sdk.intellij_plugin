package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options

import kotlinx.serialization.json.Json
import org.apache.hc.core5.http.io.entity.EntityUtils
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.dto.SrcOptionsContainer

class SrcOptionsConnector(params: ConnectorParams) : Connector(params) {
    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = ""
    private val json = Json { ignoreUnknownKeys = true }

    fun getScriptOptions(lang: String? = null): SrcOptionsContainer {
        return this.execGet(
            moduleBase + "getScriptOptions",
            paramsConst,
            mapOf("lang" to lang)
        ) { response -> json.decodeFromString(EntityUtils.toString(response.entity, Charsets.UTF_8)) }
    }

    fun getModuleOptions(): SrcOptionsContainer {
        return execGet(
            moduleBase + "getModuleOptions",
            paramsConst
        ) { response -> json.decodeFromString(EntityUtils.toString(response.entity, Charsets.UTF_8)) }
    }

    fun getAdvImportOptions(lang: String? = null): SrcOptionsContainer {
        return this.execGet(
            moduleBase + "getAdvImportOptions",
            paramsConst,
            mapOf("lang" to lang)
        ) { response -> json.decodeFromString(EntityUtils.toString(response.entity, Charsets.UTF_8)) }
    }
}