package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.openapi.components.service
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

/**
 * Добавляет поля/методы метакласса в completion независимо от DFA/inferred type.
 */
class MetaClassCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PsiElement::class.java),
            MetaClassCompletionProvider(),
        )
    }

    private class MetaClassCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.position
            if (position.language.id != "Groovy") return

            val resolveService = position.project.service<MetaClassTypeResolveService>()
            if (!resolveService.isActive()) return

            val reference = findMemberReference(position) ?: return
            val qualifier = reference.qualifierExpression ?: return

            val metaClass = resolveService.resolveMetaClassForExpression(qualifier) ?: return
            MetaClassCompletionUtil.addClassMemberLookups(metaClass, result.prefixMatcher, result)
        }

        private fun findMemberReference(position: PsiElement): GrReferenceExpression? {
            var element: PsiElement? = position
            while (element != null) {
                if (element is GrReferenceExpression && element.qualifierExpression != null) {
                    return element
                }
                element = element.parent
            }
            return null
        }
    }
}
