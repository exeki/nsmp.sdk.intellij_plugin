package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult

interface HttpRequestUseCase {
    val route: HttpRoute

    fun execute(httpExchange: HttpExchange): UseCaseResult
}
