package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.service
import com.intellij.psi.PsiType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.typeEnhancers.GrVariableEnhancer

/**
 * Подменяет тип Groovy-переменной на сгенерированный класс-заглушку по комментарию nsmp:meta.
 */
class MetaClassVariableTypeEnhancer : GrVariableEnhancer() {

    override fun getVariableType(variable: GrVariable): PsiType? =
        variable.project.service<MetaClassTypeResolveService>().resolveForVariable(variable)
}
