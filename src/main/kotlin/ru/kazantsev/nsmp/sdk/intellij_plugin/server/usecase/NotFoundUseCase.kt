package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.MessageResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult

class NotFoundUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "*",
        path = "*",
    )

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        return UseCaseResult.of(
            statusCode = 404,
            body = MessageResponse(message = "Not found"),
        )
    }
}
