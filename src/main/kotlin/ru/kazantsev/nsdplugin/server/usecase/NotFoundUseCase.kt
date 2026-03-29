package ru.kazantsev.nsdplugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsdplugin.server.HttpRoute
import ru.kazantsev.nsdplugin.server.dto.response.MessageResponse
import ru.kazantsev.nsdplugin.server.UseCaseResult

class NotFoundUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "*",
        path = "*",
    )

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        return UseCaseResult.of(
            statusCode = 404,
            body = MessageResponse(status = "error", message = "Not found"),
        )
    }
}
