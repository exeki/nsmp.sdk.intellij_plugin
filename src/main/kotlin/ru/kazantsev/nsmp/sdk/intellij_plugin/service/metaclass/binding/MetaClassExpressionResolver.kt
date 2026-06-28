package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

/**
 * Вычисляет тип Groovy-выражения по цепочке nsmp:meta и полям сгенерированных заглушек.
 */
object MetaClassExpressionResolver {

    fun resolveForReference(
        project: Project,
        reference: GrReferenceExpression,
        resolved: PsiElement? = null,
    ): PsiType? {
        val qualifier = reference.qualifierExpression
        if (qualifier == null) {
            return resolveRootReferenceType(project, reference, resolved)
        }

        val memberName = reference.referenceName ?: return null
        val qualifierClass = resolveMetaClassForExpression(project, qualifier) ?: return null
        return MetaClassMemberTypeResolver.memberType(qualifierClass, memberName)
    }

    fun resolveMetaClassForExpression(project: Project, expression: GrExpression): PsiClass? {
        if (expression is GrReferenceExpression) {
            if (expression.qualifierExpression == null) {
                resolveRootReferenceType(project, expression)?.let { type ->
                    PsiTypesUtil.getPsiClass(type)?.takeIf(MetaClassStubUtil::isGeneratedStub)?.let { return it }
                }
            } else {
                val baseClass = resolveMetaClassForExpression(project, expression.qualifierExpression!!)
                    ?: return null
                val memberName = expression.referenceName ?: return null
                val memberType = MetaClassMemberTypeResolver.memberType(baseClass, memberName) ?: return null
                return PsiTypesUtil.getPsiClass(memberType)?.takeIf(MetaClassStubUtil::isGeneratedStub)
            }
        }

        expression.type
            ?.let(PsiTypesUtil::getPsiClass)
            ?.takeIf(MetaClassStubUtil::isGeneratedStub)
            ?.let { return it }

        return null
    }

    private fun resolveRootReferenceType(
        project: Project,
        reference: GrReferenceExpression,
        resolved: PsiElement? = null,
    ): PsiType? {
        val file = reference.containingFile as? GroovyFile ?: return null
        val variableName = reference.referenceName ?: return null
        val cache = project.service<MetaClassBindingTypeCache>()
        val variableKey = file.virtualFile?.path?.let { MetaClassBindingTypeCache.VariableKey(it, variableName) }

        val metaCode = resolveMetaCode(file, reference, resolved)
            ?: variableKey?.let { cache.getRememberedMetaCode(it) }
            ?: return null

        if (variableKey != null) {
            cache.rememberMetaCode(variableKey, metaCode)
        }

        val psiClass = MetaClassPsiClassFinder.find(project, metaCode, reference) ?: return null
        return JavaPsiFacade.getInstance(project).elementFactory.createType(psiClass)
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
}
