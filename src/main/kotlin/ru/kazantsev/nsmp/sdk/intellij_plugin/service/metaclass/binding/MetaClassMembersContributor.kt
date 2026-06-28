package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.service
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil

/**
 * Добавляет члены метакласса в completion/resolve, когда DFA оставил declared type
 * (например, ISDtObject) вместо сгенерированной заглушки.
 */
class MetaClassMembersContributor : NonCodeMembersContributor() {

    override fun processDynamicElements(
        qualifierType: PsiType,
        processor: PsiScopeProcessor,
        place: PsiElement,
        state: ResolveState,
    ) {
        val resolveService = place.project.service<MetaClassTypeResolveService>()
        if (!resolveService.isActive()) return

        val qualifierReference = findQualifierExpression(place) ?: return
        val metaClass = resolveService.resolveMetaClassForExpression(qualifierReference) ?: return
        if (qualifierTypeIsMetaStub(qualifierType, metaClass)) return

        ResolveUtil.processClassDeclarations(metaClass, processor, state, place, place)
    }

    private fun findQualifierExpression(place: PsiElement): GrExpression? {
        val reference = when (place) {
            is GrReferenceExpression -> place
            else -> PsiTreeUtil.getParentOfType(place, GrReferenceExpression::class.java, false)
        } ?: return null

        reference.qualifierExpression?.let { return it }
        if (reference.qualifierExpression == null) {
            return reference
        }
        return null
    }

    private fun qualifierTypeIsMetaStub(qualifierType: PsiType, metaClass: PsiClass): Boolean {
        val qualifierClass = PsiTypesUtil.getPsiClass(qualifierType) ?: return false
        if (qualifierClass.isEquivalentTo(metaClass)) return true
        return MetaClassStubUtil.isGeneratedStub(qualifierClass)
    }
}
