package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

/**
 * Находит Groovy-переменную для ссылки, в том числе когда [GrReferenceExpression.resolve]
 * недоступен в контексте вычисления типа.
 */
object MetaClassVariableLocator {

    fun locate(
        file: GroovyFile,
        reference: GrReferenceExpression,
        resolved: PsiElement? = null,
    ): GrVariable? {
        (resolved as? GrVariable)?.let { return it }
        if (reference.qualifierExpression != null) return null

        val name = reference.referenceName ?: return null

        // Сначала ищем по документу: resolve() внутри calculateType() часто недоступен
        // и может вызвать рекурсию, из-за чего тип «мигает» при автодополнении.
        findPrecedingVariableDeclaration(file, reference, name)?.let { return it }

        (reference.resolve() as? GrVariable)?.let { return it }
        reference.advancedResolve().element.let { it as? GrVariable }?.let { return it }

        return reference.multiResolve(false)
            .asSequence()
            .mapNotNull { it.element as? GrVariable }
            .firstOrNull()
    }

    private fun findPrecedingVariableDeclaration(
        file: GroovyFile,
        reference: GrReferenceExpression,
        name: String,
    ): GrVariable? =
        PsiTreeUtil.findChildrenOfType(file, GrVariable::class.java)
            .asSequence()
            .filter { it.name == name }
            .filter { reference.textRange.startOffset >= it.textRange.startOffset }
            .maxByOrNull { it.textRange.startOffset }
}
