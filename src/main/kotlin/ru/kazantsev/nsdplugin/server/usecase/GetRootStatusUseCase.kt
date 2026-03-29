package ru.kazantsev.nsdplugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsdplugin.server.HttpRoute
import ru.kazantsev.nsdplugin.server.dto.response.MessageResponse
import ru.kazantsev.nsdplugin.server.UseCaseResult

class GetRootStatusUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "GET",
        path = "/",
    )

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        return UseCaseResult.of(
            statusCode = 200,
            body = MessageResponse(status = "ok", message = "NSD Plugin server is running"),
        )
    }
}
