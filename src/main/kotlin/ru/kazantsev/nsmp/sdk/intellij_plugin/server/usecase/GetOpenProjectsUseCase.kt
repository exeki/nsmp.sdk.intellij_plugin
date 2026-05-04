package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.project.ProjectManager
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.ProjectInfoDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.OpenProjectsResponse

class GetOpenProjectsUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "GET",
        path = "/projects",
    )

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        val projects = ProjectManager.getInstance().openProjects.map { project ->
            ProjectInfoDto.from(project)
        }

        return UseCaseResult.of(
            statusCode = 200,
            body = OpenProjectsResponse(projects = projects)
        )
    }
}

