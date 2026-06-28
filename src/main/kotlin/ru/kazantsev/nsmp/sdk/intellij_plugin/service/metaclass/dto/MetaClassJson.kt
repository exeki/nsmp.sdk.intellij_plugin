package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto

import kotlinx.serialization.json.Json

object MetaClassJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
}
