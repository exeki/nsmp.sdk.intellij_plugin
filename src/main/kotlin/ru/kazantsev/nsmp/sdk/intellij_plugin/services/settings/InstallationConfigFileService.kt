package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

import com.intellij.openapi.components.Service
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Service(Service.Level.APP)
class InstallationConfigFileService {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun load(path: String): List<ConnectorInstallation> {
        val filePath = path.toPathOrNull() ?: return emptyList()
        if (!filePath.exists() || !filePath.isRegularFile()) return emptyList()
        val content = filePath.readText(StandardCharsets.UTF_8)
        if (content.isBlank()) return emptyList()

        return runCatching { parseInstallations(content) }.getOrDefault(emptyList())
    }

    fun save(path: String, installations: List<ConnectorInstallation>) {
        val filePath = requireNotNull(path.toPathOrNull()) { MyMessageBundle.message("settings.error.config.path.invalid") }
        filePath.parent?.createDirectories()
        filePath.writeText(buildJson(installations), StandardCharsets.UTF_8)
    }

    fun upsert(path: String, installation: ConnectorInstallation): List<ConnectorInstallation> {
        val current = load(path).toMutableList()
        val index = current.indexOfFirst { it.installationId == installation.installationId }
        if (index >= 0) current[index] = installation else current.add(installation)
        save(path, current)
        return current
    }

    fun delete(path: String, installationId: String): List<ConnectorInstallation> {
        val updated = load(path).filterNot { it.installationId == installationId }
        save(path, updated)
        return updated
    }

    private fun parseInstallations(content: String): List<ConnectorInstallation> {
        val root = json.parseToJsonElement(content)
        val array = when (root) {
            is JsonArray -> root
            is JsonObject -> root["installations"]?.jsonArray ?: JsonArray(emptyList())
            else -> JsonArray(emptyList())
        }
        return array.mapNotNull { element -> parseInstallation(element) }
    }

    private fun parseInstallation(element: JsonElement): ConnectorInstallation? {
        val obj = element as? JsonObject ?: return null
        val id = obj["id"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        val scheme = obj["scheme"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        val host = obj["host"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        val accessKey = obj["accessKey"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        val ignoreSll = obj["ignoreSLL"]?.jsonPrimitive?.booleanOrNull ?: false

        if (id.isEmpty()) return null
        return ConnectorInstallation(
            installationId = id,
            scheme = scheme,
            host = host,
            accessKey = accessKey,
            ignoreSSL = ignoreSll,
        )
    }

    private fun buildJson(installations: List<ConnectorInstallation>): String {
        val root = buildJsonObject {
            put("installations", buildJsonArray {
                installations.forEach { installation ->
                    add(
                        buildJsonObject {
                            put("id", JsonPrimitive(installation.installationId))
                            put("scheme", JsonPrimitive(installation.scheme))
                            put("host", JsonPrimitive(installation.host))
                            put("accessKey", JsonPrimitive(installation.accessKey))
                            put("ignoreSLL", JsonPrimitive(installation.ignoreSSL))
                        }
                    )
                }
            })
        }
        return json.encodeToString(JsonObject.serializer(), root)
    }

    private fun String.toPathOrNull(): Path? {
        return runCatching { Paths.get(trim()) }
            .getOrNull()
            ?.takeIf { it.toString().isNotBlank() }
    }
}
