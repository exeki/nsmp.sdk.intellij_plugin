package ru.kazantsev.nsmp.sdk.intellij_plugin.server.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase.NotFoundUseCase

@Service(Service.Level.APP)
class NsdHttpRequestHandlerService {
    private val useCaseRegistry: HttpRequestUseCaseRegistry
        get() = ApplicationManager.getApplication().getService(HttpRequestUseCaseRegistry::class.java)

    private val notFoundUseCase = NotFoundUseCase()

    fun handle(exchange: HttpExchange): UseCaseResult {
        val route = HttpRoute(method = exchange.requestMethod, path = exchange.requestURI.path)
        val useCase = useCaseRegistry.find(route) ?: notFoundUseCase
        return useCase.execute(exchange)
    }
}
