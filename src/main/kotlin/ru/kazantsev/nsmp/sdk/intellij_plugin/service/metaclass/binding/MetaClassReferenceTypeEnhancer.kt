package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.typeEnhancers.GrReferenceTypeEnhancer

/**
 * Подменяет тип ссылки на переменную с nsmp:meta-комментарием при доступе через точку.
 */
class MetaClassReferenceTypeEnhancer : GrReferenceTypeEnhancer() {

    override fun getReferenceType(reference: GrReferenceExpression, resolved: PsiElement?): PsiType? =
        reference.project.service<MetaClassTypeResolveService>().resolveForReference(reference, resolved)
}
