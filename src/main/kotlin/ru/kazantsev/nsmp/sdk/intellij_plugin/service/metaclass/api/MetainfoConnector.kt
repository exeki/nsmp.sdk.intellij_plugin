package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.api

import kotlinx.serialization.json.Json
import org.apache.hc.core5.http.io.entity.EntityUtils
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassJson

/**
 * HTTP-клиент для получения метаинформации метаклассов через sdk_controller.
 */
class MetainfoConnector(params: ConnectorParams) : Connector(params) {

    private val json = MetaClassJson.instance

    fun getMetaClassBranchInfo(metas: List<String>): List<MetaClassDto> =
        execGet(
            MODULE_BASE + "getMetaClassBranchInfo",
            "",
            mapOf("metas" to metas.joinToString(",")),
        ) { response ->
            json.decodeFromString(EntityUtils.toString(response.entity, Charsets.UTF_8))
        }

    fun getMetaClassBranchesInfo(metas: List<String>): List<MetaClassDto> =
        execGet(
            MODULE_BASE + "getMetaClassBranchesInfo",
            "",
            mapOf("metas" to metas.joinToString(",")),
        ) { response ->
            json.decodeFromString(EntityUtils.toString(response.entity, Charsets.UTF_8))
        }

    fun getMetaClassInfo(meta: String): MetaClassDto =
        execGet(
            MODULE_BASE + "getMetaClassInfo",
            "",
            mapOf("meta" to meta),
        ) { response ->
            json.decodeFromString(EntityUtils.toString(response.entity, Charsets.UTF_8))
        }

    private companion object {
        const val MODULE_BASE = "modules.sdkController."
    }
}
