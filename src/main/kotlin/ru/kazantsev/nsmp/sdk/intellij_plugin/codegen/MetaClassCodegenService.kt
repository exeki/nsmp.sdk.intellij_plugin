package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.squareup.javapoet.JavaFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding.MetaClassBindingTypeCache
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

/**
 * Генерирует Java-файлы заглушек метаклассов и регистрирует каталог generated sources.
 */
@Service(Service.Level.PROJECT)
class MetaClassCodegenService(private val project: Project) {

    fun generateAll(metaClasses: List<MetaClassDto>) {
        if (metaClasses.isEmpty()) return

        ApplicationManager.getApplication().invokeAndWait {
            WriteAction.run<RuntimeException> {
                val outputDirectory = generatedSourceRootService.getOutputDirectoryPath()
                cleanOutputDirectory(outputDirectory)

                val registry = MetaClassRegistry(metaClasses)
                val typeSpecFactory = MetaClassTypeSpecFactory(registry)
                metaClasses.forEach { metaClass ->
                    val typeSpec = typeSpecFactory.create(metaClass)
                    try {
                        JavaFile.builder(MetaClassGenerationSettings.PACKAGE_NAME, typeSpec)
                            .build()
                            .writeTo(outputDirectory.toFile())
                    } catch (error: IOException) {
                        throw RuntimeException(
                            "Failed to generate class for metaclass ${metaClass.fullCode}",
                            error,
                        )
                    }
                }
                generatedSourceRootService.ensureRegistered()
                project.service<MetaClassBindingTypeCache>().clear()
            }
        }
    }

    private val generatedSourceRootService = project.service<GeneratedSourceRootService>()

    private fun cleanOutputDirectory(outputDirectory: Path) {
        Files.createDirectories(outputDirectory)
        if (!Files.exists(outputDirectory)) return
        outputDirectory.walk()
            .filter { it.isRegularFile() && it.fileName.toString().endsWith(".java") }
            .forEach { it.deleteIfExists() }
    }
}
