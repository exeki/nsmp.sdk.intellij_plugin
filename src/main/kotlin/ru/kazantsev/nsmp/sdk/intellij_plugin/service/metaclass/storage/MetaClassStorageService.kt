package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.storage

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassJson
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import java.nio.file.Files
import java.nio.file.Path

/**
 * Читает и сохраняет кэш метаклассов в JSON внутри `.nsmp_sdk/metainfo`.
 */
@Service(Service.Level.PROJECT)
class MetaClassStorageService(private val project: Project) {

    private val json = Json(from = MetaClassJson.instance) {
        prettyPrint = true
    }

    val storageFilePath: Path
        get() {
            val basePath = project.basePath ?: throw IllegalStateException("Project base path is not set")
            return Path.of(basePath, Constants.SDK_DIR, METAINFO_DIR, METACLASSES_FILE)
        }

    fun load(): List<MetaClassDto> {
        val file = storageFilePath
        if (!Files.exists(file)) return emptyList()
        return json.decodeFromString(Files.readString(file))
    }

    fun save(metaClasses: List<MetaClassDto>) {
        val file = storageFilePath
        Files.createDirectories(file.parent)
        Files.writeString(file, json.encodeToString(metaClasses))
    }

    fun findByFullCode(fullCode: String): MetaClassDto? =
        load().find { it.fullCode == fullCode }

    private companion object {
        const val METAINFO_DIR = "metainfo"
        const val METACLASSES_FILE = "metaclasses.json"
    }
}
