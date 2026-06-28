package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService

@Service(Service.Level.PROJECT)
class MetaClassTypeResolveService(private val project: Project) {

    fun isActive(): Boolean = project.service<PluginActivationService>().pluginActive

    fun resolveForVariable(variable: GrVariable): PsiType? {
        if (!isActive()) return null
        val file = variable.containingFile as? GroovyFile ?: return null
        val metaCode = MetaClassCommentParser.findMetaCodeForVariable(file, variable) ?: return null
        return resolveMetaClassType(metaCode, variable)
    }

    fun resolveForReference(reference: GrReferenceExpression, resolved: PsiElement? = null): PsiType? {
        if (!isActive()) return null
        return MetaClassExpressionResolver.resolveForReference(project, reference, resolved)
    }

    fun resolveMetaClass(reference: GrReferenceExpression, resolved: PsiElement? = null): PsiClass? {
        if (!isActive()) return null
        if (reference.qualifierExpression != null) return null
        val file = reference.containingFile as? GroovyFile ?: return null
        val metaCode = resolveMetaCode(file, reference, resolved) ?: return null
        return MetaClassPsiClassFinder.find(project, metaCode, reference)
    }

    fun resolveMetaClassForExpression(expression: GrExpression): PsiClass? {
        if (!isActive()) return null
        return MetaClassExpressionResolver.resolveMetaClassForExpression(project, expression)
    }

    private fun resolveMetaCode(
        file: GroovyFile,
        reference: GrReferenceExpression,
        resolved: PsiElement?,
    ): String? {
        MetaClassVariableLocator.locate(file, reference, resolved)
            ?.let { variable -> MetaClassCommentParser.findMetaCodeForVariable(file, variable) }
            ?.let { return it }

        MetaClassCommentParser.findMetaCodeForReference(file, reference)?.let { return it }

        val name = reference.referenceName ?: return null
        return MetaClassCommentParser.findMetaCode(file, name)
    }

    private fun resolveMetaClassType(metaCode: String, context: PsiElement): PsiType? {
        val psiClass = MetaClassPsiClassFinder.find(project, metaCode, context) ?: return null
        return JavaPsiFacade.getInstance(project).elementFactory.createType(psiClass)
    }
}
