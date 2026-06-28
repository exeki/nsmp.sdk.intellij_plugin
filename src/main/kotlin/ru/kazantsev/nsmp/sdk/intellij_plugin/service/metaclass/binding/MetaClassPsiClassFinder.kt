package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import ru.kazantsev.nsmp.sdk.intellij_plugin.codegen.MetaClassGenerationSettings
import ru.kazantsev.nsmp.sdk.intellij_plugin.codegen.MetaClassNaming
import java.nio.file.Path

/**
 * Находит сгенерированный PsiClass метакласса через индекс и напрямую по файлу в `.nsmp_sdk`.
 */
object MetaClassPsiClassFinder {

    fun find(project: Project, metaCode: String, context: PsiElement? = null): PsiClass? {
        val qualifiedName = MetaClassNaming.qualifiedClassName(metaCode)
        val simpleName = qualifiedName.substringAfterLast('.')

        findViaIndex(project, qualifiedName, context)?.let { return it }
        findInGeneratedSourceFile(project, qualifiedName, simpleName)?.let { return it }
        return null
    }

    private fun findViaIndex(project: Project, qualifiedName: String, context: PsiElement?): PsiClass? {
        val facade = JavaPsiFacade.getInstance(project)
        for (scope in searchScopes(project, context)) {
            facade.findClass(qualifiedName, scope)?.let { return it }
        }
        return null
    }

    private fun findInGeneratedSourceFile(project: Project, qualifiedName: String, simpleName: String): PsiClass? {
        val basePath = project.basePath ?: return null
        val packagePath = qualifiedName.substringBeforeLast('.').replace('.', '/')
        val relativePath = listOf(
            MetaClassGenerationSettings.GENERATED_SOURCES_RELATIVE_PATH,
            packagePath,
            "$simpleName.java",
        ).joinToString("/")

        val virtualFile = LocalFileSystem.getInstance().findFileByPath(
            Path.of(basePath, relativePath).toString().replace('\\', '/'),
        ) ?: return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? PsiJavaFile ?: return null
        return psiFile.classes.firstOrNull { it.name == simpleName }
    }

    private fun searchScopes(project: Project, context: PsiElement?): List<GlobalSearchScope> {
        val projectScope = GlobalSearchScope.projectScope(project)
        val allScope = GlobalSearchScope.allScope(project)
        val module = context?.let(ModuleUtilCore::findModuleForPsiElement)
        if (module == null) {
            return listOf(projectScope, allScope)
        }
        return listOf(
            GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module),
            projectScope,
            allScope,
        )
    }
}
