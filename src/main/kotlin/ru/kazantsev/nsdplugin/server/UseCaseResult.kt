package ru.kazantsev.nsdplugin.server

import kotlinx.serialization.json.JsonElement

data class UseCaseResult(
    val statusCode: Int,
    val body: JsonElement,
) {
    companion object {
        inline fun <reified T> of(statusCode: Int, body: T): UseCaseResult {
            return UseCaseResult(
                statusCode = statusCode,
                body = JsonSerializer.toJsonElement(body),
            )
        }
    }
}
