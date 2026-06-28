package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.sync

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.api.MetainfoConnector
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.codegen.MetaClassCodegenService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.storage.MetaClassStorageService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

/**
 * Загружает метаклассы с инсталляции, сохраняет JSON и запускает перегенерацию заглушек.
 */
@Service(Service.Level.PROJECT)
class MetaClassSyncService(private val project: Project) {

    private val projectSettingsService = project.service<ProjectSettingsService>()
    private val storageService = project.service<MetaClassStorageService>()
    private val codegenService = project.service<MetaClassCodegenService>()

    fun syncDefaultBranches(): List<MetaClassDto> =
        syncBranches(DEFAULT_ROOT_METAS)

    fun syncBranches(rootMetaCodes: Collection<String>): List<MetaClassDto> {
        val connector = MetainfoConnector(projectSettingsService.connectorParams)
        val metaClasses = connector.getMetaClassBranchesInfo(rootMetaCodes.toList())
        persistAndGenerate(metaClasses)
        return metaClasses
    }

    fun syncSingleMetaClass(metaCode: String): MetaClassDto {
        val connector = MetainfoConnector(projectSettingsService.connectorParams)
        val metaClass = connector.getMetaClassInfo(metaCode)
        val merged = storageService.load()
            .filterNot { it.fullCode == metaClass.fullCode }
            .plus(metaClass)
        persistAndGenerate(merged)
        return metaClass
    }

    fun regenerateFromStorage() {
        val metaClasses = storageService.load()
        if (metaClasses.isNotEmpty()) {
            codegenService.generateAll(metaClasses)
        }
    }

    private fun persistAndGenerate(metaClasses: List<MetaClassDto>) {
        storageService.save(metaClasses)
        codegenService.generateAll(metaClasses)
    }

    private companion object {
        val DEFAULT_ROOT_METAS = listOf("catalogItem", "abstractSysObj", "abstractBO")
    }
}
