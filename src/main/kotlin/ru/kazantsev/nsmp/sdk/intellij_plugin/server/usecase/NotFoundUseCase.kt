package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.MessageResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class NotFoundUseCase : AbstractHttpRequestUseCase() {
    override val route: HttpRoute = HttpRoute(
        method = "*",
        path = "*",
    )

    override fun executeInternal(httpExchange: HttpExchange): UseCaseResult {
        return UseCaseResult.of(
            statusCode = 404,
            body = MessageResponse(message = MessageBundle.message("api.error.not.found")),
        )
    }
}
