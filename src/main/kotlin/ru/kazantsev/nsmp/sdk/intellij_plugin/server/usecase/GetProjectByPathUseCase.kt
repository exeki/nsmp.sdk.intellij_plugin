package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.ProjectUtilities
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.request.ProjectByPathRequest
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.ProjectByPathResponse

class GetProjectByPathUseCase : AbstractHttpRequestUseCase() {
    override val route: HttpRoute = HttpRoute(
        method = "POST",
        path = "/projects/by-path",
    )

    override fun executeInternal(httpExchange: HttpExchange): UseCaseResult {

        val request = getBodyElseThrow<ProjectByPathRequest>(httpExchange)

        val requestedPath = request.path

        val project = ProjectUtilities.findProjectByPathElseThrow(requestedPath)

        return UseCaseResult.of(
            statusCode = 200,
            body = ProjectByPathResponse.from(project),
        )
    }
}
