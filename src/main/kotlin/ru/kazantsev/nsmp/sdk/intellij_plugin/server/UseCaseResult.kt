package ru.kazantsev.nsmp.sdk.intellij_plugin.server

import kotlinx.serialization.json.JsonElement
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.MessageResponse

data class UseCaseResult(
    val statusCode: Int,
    val body: JsonElement,
) {

    companion object {

        fun success(successMessage: String): UseCaseResult {
            return of(200, MessageResponse(message = successMessage))
        }

        inline fun <reified T> of(statusCode: Int, body: T): UseCaseResult {
            return UseCaseResult(
                statusCode = statusCode,
                body = JsonSerializer.toJsonElement(body),
            )
        }

        fun of(e: Throwable): UseCaseResult {
            return if (e is UseCaseExecutionException)
                of(e.code, MessageResponse(message = e.message))
            else
                of(500, MessageResponse(message = e.message ?: "No message"))
        }
    }
}
