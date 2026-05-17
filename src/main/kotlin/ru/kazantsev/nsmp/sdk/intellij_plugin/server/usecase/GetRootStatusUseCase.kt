package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult

class GetRootStatusUseCase : AbstractHttpRequestUseCase() {
    override val route: HttpRoute = HttpRoute(
        method = "GET",
        path = "/",
    )

    override fun executeInternal(httpExchange: HttpExchange): UseCaseResult {
        return UseCaseResult.success("NSD Plugin server is running")
    }
}
