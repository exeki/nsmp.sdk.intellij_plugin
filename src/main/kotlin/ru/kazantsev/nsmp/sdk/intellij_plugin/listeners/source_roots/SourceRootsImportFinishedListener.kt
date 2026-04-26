package ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.source_roots

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.project.SourceRootMarkerService

@Service(Service.Level.PROJECT)
class SourceRootsImportFinishedListener(
    project: Project
) : ProjectDataImportListener, Disposable {
    private val sourceRootMarkerService = project.service<SourceRootMarkerService>()
    private val busConnection = project.messageBus.connect(this)

    init {
        busConnection.subscribe(ProjectDataImportListener.TOPIC, this)
    }

    override fun onImportFinished(projectPath: String?) {
        logger.info("Build system sync finished for '${projectPath.orEmpty()}', refreshing source/resource roots")
        sourceRootMarkerService.markConfiguredRoots()
    }

    override fun dispose() = busConnection.dispose()

    private companion object {
        private val logger = thisLogger()
    }
}
