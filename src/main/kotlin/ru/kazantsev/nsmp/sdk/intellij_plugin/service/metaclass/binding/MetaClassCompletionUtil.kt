package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import org.jetbrains.plugins.groovy.lang.completion.CompleteReferenceExpression

object MetaClassCompletionUtil {

    fun addClassMemberLookups(
        metaClass: PsiClass,
        prefixMatcher: PrefixMatcher,
        result: CompletionResultSet,
    ) {
        for (field in metaClass.allFields) {
            if (field.hasModifierProperty(PsiModifier.STATIC)) continue
            val name = field.name ?: continue
            if (!prefixMatcher.prefixMatches(name)) continue
            result.addElement(CompleteReferenceExpression.createPropertyLookupElement(name, field.type))
        }

        for (method in metaClass.methods) {
            if (method.hasModifierProperty(PsiModifier.STATIC)) continue
            val name = method.name ?: continue
            if (name == "class" || name == "wait" || name == "notify" || name == "notifyAll") continue
            if (!prefixMatcher.prefixMatches(name)) continue
            result.addElement(
                LookupElementBuilder.create(method)
                    .withPresentableText(name),
            )
        }
    }
}
