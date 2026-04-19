package ru.kazantsev.nsmp.sdk.intellij_plugin.services.init

import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

data class GradleInitResult(
    val buildUpdated: Boolean,
    val settingsUpdated: Boolean,
) {
    val changed: Boolean get() = buildUpdated || settingsUpdated
}

object GradleProjectInitializer {

    fun isProjectInitialized(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val root = Path.of(basePath)
        return buildFiles(root).any { file ->
            file.exists() && file.isRegularFile() && containsPlugin(file.readText(StandardCharsets.UTF_8))
        }
    }

    fun initialize(project: Project): GradleInitResult {
        val basePath = project.basePath
            ?: throw IllegalStateException(MessageBundle.message("init.error.project.path.missing"))
        val root = Path.of(basePath)

        val buildFile = resolveBuildFile(root)
            ?: throw IllegalStateException(MessageBundle.message("init.error.build.file.missing"))
        val settingsFile = resolveSettingsFile(root, buildFile.name.endsWith(".kts"))

        val buildOriginal = buildFile.readText(StandardCharsets.UTF_8)
        val buildUpdated = ensurePlugin(buildOriginal, buildFile.name.endsWith(".kts"))
        if (buildUpdated.second) {
            buildFile.writeText(buildUpdated.first, StandardCharsets.UTF_8)
        }

        val settingsOriginal = if (settingsFile.exists()) {
            settingsFile.readText(StandardCharsets.UTF_8)
        } else {
            settingsFile.parent?.createDirectories()
            ""
        }
        val settingsUpdated = ensureRepository(settingsOriginal, settingsFile.name.endsWith(".kts"))
        if (settingsUpdated.second) {
            settingsFile.writeText(settingsUpdated.first, StandardCharsets.UTF_8)
        }

        return GradleInitResult(
            buildUpdated = buildUpdated.second,
            settingsUpdated = settingsUpdated.second
        )
    }

    private fun resolveBuildFile(root: Path): Path? {
        val kts = root.resolve("build.gradle.kts")
        if (kts.exists() && kts.isRegularFile()) return kts
        val groovy = root.resolve("build.gradle")
        if (groovy.exists() && groovy.isRegularFile()) return groovy
        return null
    }

    private fun resolveSettingsFile(root: Path, preferKts: Boolean): Path {
        val kts = root.resolve("settings.gradle.kts")
        val groovy = root.resolve("settings.gradle")
        if (kts.exists() && kts.isRegularFile()) return kts
        if (groovy.exists() && groovy.isRegularFile()) return groovy
        return if (preferKts) kts else groovy
    }

    private fun buildFiles(root: Path): List<Path> {
        return listOf(root.resolve("build.gradle.kts"), root.resolve("build.gradle"))
    }

    private fun containsPlugin(content: String): Boolean {
        if (content.isBlank()) return false
        val escapedId = Regex.escape(GradleInitConfig.pluginId)
        val pluginRegex = Regex("""id\s*(\(|\s+)['"]$escapedId['"]""")
        val applyRegex = Regex("""apply\s+plugin:\s*['"]$escapedId['"]""")
        return pluginRegex.containsMatchIn(content) || applyRegex.containsMatchIn(content)
    }

    private fun ensurePlugin(content: String, isKts: Boolean): Pair<String, Boolean> {
        if (containsPlugin(content)) return content to false

        val newline = detectNewline(content)
        val pluginLine = if (isKts) {
            """id("${GradleInitConfig.pluginId}") version "${GradleInitConfig.pluginVersion}""""
        } else {
            """id '${GradleInitConfig.pluginId}' version '${GradleInitConfig.pluginVersion}'"""
        }

        val pluginsBlock = Regex("""(?m)^(\s*)plugins\s*\{""").find(content)
        if (pluginsBlock != null) {
            val indent = pluginsBlock.groupValues[1]
            val openBraceIndex = content.indexOf('{', pluginsBlock.range.first)
            val insertion = "$newline$indent    $pluginLine"
            val updated = content.substring(0, openBraceIndex + 1) + insertion + content.substring(openBraceIndex + 1)
            return updated to true
        }

        val prefix = buildString {
            append("plugins {")
            append(newline)
            append("    ")
            append(pluginLine)
            append(newline)
            append("}")
            append(newline)
            append(newline)
        }
        return (prefix + content) to true
    }

    private fun ensureRepository(content: String, isKts: Boolean): Pair<String, Boolean> {
        if (content.contains(GradleInitConfig.pluginRepositoryUrl)) return content to false

        val newline = detectNewline(content)
        val repoBlock = if (isKts) {
            buildString {
                append("""maven(url = uri("${GradleInitConfig.pluginRepositoryUrl}")) {""")
                append(newline)
                append("            credentials {")
                append(newline)
                append("                username = ${GradleInitConfig.pluginRepositoryUsernameExpr}")
                append(newline)
                append("                password = ${GradleInitConfig.pluginRepositoryPasswordExpr}")
                append(newline)
                append("            }")
                append(newline)
                append("        }")
            }
        } else {
            buildString {
                append("maven {")
                append(newline)
                append("            url = uri('${GradleInitConfig.pluginRepositoryUrl}')")
                append(newline)
                append("            credentials {")
                append(newline)
                append("                username = ${GradleInitConfig.pluginRepositoryUsernameExpr}")
                append(newline)
                append("                password = ${GradleInitConfig.pluginRepositoryPasswordExpr}")
                append(newline)
                append("            }")
                append(newline)
                append("        }")
            }
        }
        val pluginManagementMatch = Regex("""(?m)^(\s*)pluginManagement\s*\{""").find(content)
        if (pluginManagementMatch == null) {
            val block = buildString {
                append("pluginManagement {")
                append(newline)
                append("    repositories {")
                append(newline)
                append("        ")
                append(repoBlock)
                append(newline)
                append("        gradlePluginPortal()")
                append(newline)
                append("    }")
                append(newline)
                append("}")
                append(newline)
                append(newline)
            }
            return (block + content) to true
        }

        val pmOpenBrace = content.indexOf('{', pluginManagementMatch.range.first)
        if (pmOpenBrace < 0) return content to false
        val pmCloseBrace = findMatchingBrace(content, pmOpenBrace)
        if (pmCloseBrace < 0) return content to false

        val pmBody = content.substring(pmOpenBrace + 1, pmCloseBrace)
        val repositoriesMatch = Regex("""(?m)^(\s*)repositories\s*\{""").find(pmBody)
        if (repositoriesMatch == null) {
            val pmIndent = pluginManagementMatch.groupValues[1]
            val insertion = buildString {
                append(newline)
                append(pmIndent)
                append("    repositories {")
                append(newline)
                append(pmIndent)
                append("        ")
                append(repoBlock.replace(newline, "$newline$pmIndent"))
                append(newline)
                append(pmIndent)
                append("    }")
            }
            val updated = content.substring(0, pmOpenBrace + 1) + insertion + content.substring(pmOpenBrace + 1)
            return updated to true
        }

        val repoIndent = repositoriesMatch.groupValues[1]
        val repositoriesStart = pmOpenBrace + 1 + repositoriesMatch.range.first
        val repositoriesOpenBrace = content.indexOf('{', repositoriesStart)
        if (repositoriesOpenBrace < 0) return content to false
        val insertion = buildString {
            append(newline)
            append(repoIndent)
            append("    ")
            append(repoBlock.replace(newline, "$newline$repoIndent    "))
        }
        val updated = content.substring(0, repositoriesOpenBrace + 1) + insertion + content.substring(repositoriesOpenBrace + 1)
        return updated to true
    }

    private fun detectNewline(content: String): String {
        return if (content.contains("\r\n")) "\r\n" else "\n"
    }

    private fun findMatchingBrace(content: String, openBraceIndex: Int): Int {
        var depth = 0
        for (i in openBraceIndex until content.length) {
            when (content[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }
}
