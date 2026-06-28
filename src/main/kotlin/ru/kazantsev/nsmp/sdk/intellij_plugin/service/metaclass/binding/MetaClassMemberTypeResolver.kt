package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType

/**
 * Возвращает тип поля/геттера сгенерированной заглушки метакласса.
 */
object MetaClassMemberTypeResolver {

    fun memberType(psiClass: PsiClass, memberName: String): PsiType? {
        psiClass.findFieldByName(memberName, true)?.type?.let { return it }

        var current: PsiClass? = psiClass
        while (current != null) {
            current.methods.asSequence()
                .filter { !it.hasModifierProperty(PsiModifier.STATIC) }
                .firstOrNull { it.name == memberName || it.name == getterName(memberName) }
                ?.returnType
                ?.let { return it }
            current = current.superClass
        }
        return null
    }

    private fun getterName(fieldName: String): String =
        "get${fieldName.replaceFirstChar { char -> char.uppercaseChar() }}"
}
