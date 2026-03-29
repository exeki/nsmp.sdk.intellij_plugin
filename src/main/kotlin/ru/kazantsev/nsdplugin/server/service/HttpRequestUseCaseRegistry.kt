package ru.kazantsev.nsdplugin.server.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import ru.kazantsev.nsdplugin.server.HttpRoute
import ru.kazantsev.nsdplugin.server.usecase.CreateGroovyFileUseCase
import ru.kazantsev.nsdplugin.server.usecase.GetCurrentProjectsUseCase
import ru.kazantsev.nsdplugin.server.usecase.GetRootStatusUseCase
import ru.kazantsev.nsdplugin.server.usecase.HttpRequestUseCase

@Service(Service.Level.APP)
class HttpRequestUseCaseRegistry {

    private val useCasesByRoute: Map<HttpRoute, HttpRequestUseCase> = listOf(
        GetRootStatusUseCase(),
        GetCurrentProjectsUseCase(),
        CreateGroovyFileUseCase(),
    ).associateBy { it.route }

    fun find(route: HttpRoute): HttpRequestUseCase? {
        logger.info("NSD Plugin searching use case for method=${route.method}, path=${route.path}")
        val useCase = useCasesByRoute[route]

        if (useCase == null) logger.info("NSD Plugin use case not found for method=${route.method}, path=${route.path}")
        else logger.info("NSD Plugin use case found: ${useCase::class.simpleName} for method=${route.method}, path=${route.path}")


        return useCase
    }

    private companion object {
        private val logger = thisLogger()
    }
}
