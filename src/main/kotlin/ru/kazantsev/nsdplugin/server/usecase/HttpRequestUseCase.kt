package ru.kazantsev.nsdplugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsdplugin.server.HttpRoute
import ru.kazantsev.nsdplugin.server.UseCaseResult

interface HttpRequestUseCase {
    val route: HttpRoute

    fun execute(httpExchange: HttpExchange): UseCaseResult
}
