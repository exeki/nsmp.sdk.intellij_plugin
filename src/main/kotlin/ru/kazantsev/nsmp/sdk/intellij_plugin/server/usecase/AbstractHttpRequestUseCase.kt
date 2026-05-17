package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.diagnostic.thisLogger
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.JsonSerializer
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseExecutionException
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import java.nio.charset.StandardCharsets

abstract class AbstractHttpRequestUseCase {
    abstract val route: HttpRoute
    private val logger = thisLogger()

    fun execute(httpExchange: HttpExchange): UseCaseResult {
        return try {
            logger.info("Executing http use case: ${route.method} ${route.path}")
            executeInternal(httpExchange)
        } catch (ex: Throwable) {
            UseCaseResult.of(ex)
        }
    }

    abstract fun executeInternal(httpExchange: HttpExchange): UseCaseResult

    fun queryParams(httpExchange: HttpExchange): Map<String, String> {
        val query = httpExchange.requestURI.path
        val segments = query.split('?')
        return if (segments.size != 1) segments.last()
            .split('&')
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) null
                else part.substring(0, idx) to part.substring(idx + 1)
            }.toMap()
        else emptyMap()
    }

    fun getParamElseThrow(httpExchange: HttpExchange, param: String): String {
        val params = queryParams(httpExchange)
        val paramValue = params[param]
        if (paramValue.isNullOrBlank())
            throw UseCaseExecutionException(400, "Parameter '$param' is missing")
        else return paramValue
    }

    fun getBody(httpExchange: HttpExchange): String? {
        val rawBody = httpExchange.requestBody.use { it.readAllBytes() }.toString(StandardCharsets.UTF_8)
        if (rawBody.isBlank()) return null
        else return rawBody
    }

    inline fun <reified T> getBody(httpExchange: HttpExchange): T? {
        val rawBody = getBody(httpExchange) ?: return null
        return try {
            JsonSerializer.fromJson<T>(rawBody)
        } catch (e: Exception) {
            throw UseCaseExecutionException(400, "Invalid JSON body: ${e.message}")
        }
    }

    inline fun <reified T> getBodyElseThrow(httpExchange: HttpExchange): T {
        val body = getBody<T>(httpExchange) ?: throw UseCaseExecutionException(400, "Body is missing")
        return body
    }

}

