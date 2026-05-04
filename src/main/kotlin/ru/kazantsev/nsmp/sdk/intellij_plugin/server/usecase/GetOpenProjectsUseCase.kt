package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.ProjectInfoDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.ProjectPluginInfoDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.OpenProjectsResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

class GetOpenProjectsUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "GET",
        path = "/projects",
    )

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        val projects = ProjectManager.getInstance().openProjects.map { project ->
            val projectSettingsService = project.service<ProjectSettingsService>()
            val installationIsSpecified = projectSettingsService.installationIsSpecified()
            val pluginInfo = if(!installationIsSpecified) null
            else {
                val connectorParams = projectSettingsService.connectorParams
                ProjectPluginInfoDto(
                    installationId = connectorParams.installationId,
                    installationHost = connectorParams.host
                )
            }
            ProjectInfoDto(
                name = project.name,
                basePath = project.basePath,
                isDisposed = project.isDisposed,
                isInitialized = project.isInitialized,
                nsmpSdk = pluginInfo
            )
        }

        return UseCaseResult.of(
            statusCode = 200,
            body = OpenProjectsResponse(projects = projects)
        )
    }
}

