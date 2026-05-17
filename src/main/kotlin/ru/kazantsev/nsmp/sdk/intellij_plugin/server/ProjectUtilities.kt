package ru.kazantsev.nsmp.sdk.intellij_plugin.server

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.SystemInfo
import java.nio.file.InvalidPathException
import java.nio.file.Path

class ProjectUtilities {

    companion object {
        fun findProjectByPath(requestedPath: String): Project? {
            return ProjectManager.getInstance().openProjects.firstOrNull { p ->
                sameProjectPath(p.basePath, requestedPath)
            }
        }

        fun  findProjectByPathElseThrow(requestedPath: String): Project {
            return findProjectByPath(requestedPath) ?: throw UseCaseExecutionException(400, "No open project matches the given path")
        }

        private fun sameProjectPath(projectBasePath: String?, requestedPath: String): Boolean {
            if (projectBasePath.isNullOrBlank()) return false
            val nRequested = normalizePath(requestedPath)
            val nBase = normalizePath(projectBasePath)
            return if (SystemInfo.isWindows) nRequested.equals(nBase, ignoreCase = true)
            else nRequested == nBase
        }

        private fun normalizePath(path: String): String {
            return try {
                Path.of(path).normalize().toString().replace('\\', '/').trimEnd('/')
            } catch (_: InvalidPathException) {
                path.trim().replace('\\', '/').trimEnd('/')
            }
        }
    }

}