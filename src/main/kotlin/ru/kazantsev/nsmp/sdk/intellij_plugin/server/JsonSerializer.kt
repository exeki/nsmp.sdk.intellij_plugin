package ru.kazantsev.nsmp.sdk.intellij_plugin.server

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object JsonSerializer {
    @PublishedApi
    internal val json = Json {
        prettyPrint = false
        explicitNulls = true
        encodeDefaults = true
    }

    inline fun <reified T> toJson(value: T): String = json.encodeToString(value)

    inline fun <reified T> toJsonElement(value: T): JsonElement = json.parseToJsonElement(toJson(value))
}
