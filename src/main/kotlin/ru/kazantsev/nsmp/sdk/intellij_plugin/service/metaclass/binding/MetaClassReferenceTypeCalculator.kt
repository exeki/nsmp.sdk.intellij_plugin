package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.service
import com.intellij.psi.PsiType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.typing.GrTypeCalculator

/**
 * Подменяет тип ссылки на переменную с nsmp:meta-комментарием до расчёта declared type.
 */
class MetaClassReferenceTypeCalculator : GrTypeCalculator<GrReferenceExpression> {

    override fun getType(expression: GrReferenceExpression): PsiType? =
        expression.project.service<MetaClassTypeResolveService>().resolveForReference(expression)
}
