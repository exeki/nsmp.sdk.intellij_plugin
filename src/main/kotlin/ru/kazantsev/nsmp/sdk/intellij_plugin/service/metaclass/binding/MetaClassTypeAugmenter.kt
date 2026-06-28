package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.service
import com.intellij.psi.PsiType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.dataFlow.types.TypeAugmenter

/**
 * Подменяет тип переменной с nsmp:meta-комментарием в DFA/inference-контексте.
 */
class MetaClassTypeAugmenter : TypeAugmenter() {

    override fun inferType(variable: GrVariable): PsiType? =
        variable.project.service<MetaClassTypeResolveService>().resolveForVariable(variable)
}
