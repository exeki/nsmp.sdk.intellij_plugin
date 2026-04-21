package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options

import kotlinx.serialization.json.Json
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.io.entity.EntityUtils
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOptionsContainer

class SrcOptionsConnector(params: ConnectorParams) : Connector(params) {
    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"
    private val json = Json {
        ignoreUnknownKeys = true
    }


    fun getScriptOptions(lang: String? = null): SrcOptionsContainer {
        val response = this.execGet(
            moduleBase + "getScriptOptions",
            paramsConst,
            mapOf("lang" to lang)
        )
        BadResponseException.throwIfNotOk(this, response)
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return json.decodeFromString(bodyText)
    }

    fun getModuleOptions(): SrcOptionsContainer {
        val response = this.execGet(
            moduleBase + "getModuleOptions",
            paramsConst,
            null
        )
        BadResponseException.throwIfNotOk(this, response)
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return json.decodeFromString(bodyText)
    }

    fun getAdvImportOptions(lang: String? = null): SrcOptionsContainer {
        val response = this.execGet(
            moduleBase + "getAdvImportOptions",
            paramsConst,
            mapOf("lang" to lang)
        )
        BadResponseException.throwIfNotOk(this, response)
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return json.decodeFromString(bodyText)
    }
}