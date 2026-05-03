package ru.kazantsev.nsmp.sdk.intellij_plugin.unused_concepts.source_roots

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.project.Project

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
        sourceRootMarkerService.markConfiguredRoots()
    }

    override fun dispose() = busConnection.dispose()

}
