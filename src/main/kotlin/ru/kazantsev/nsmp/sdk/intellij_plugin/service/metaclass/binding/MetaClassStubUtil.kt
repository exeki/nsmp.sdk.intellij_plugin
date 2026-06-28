package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.codegen.MetaClassGenerationSettings

object MetaClassStubUtil {

    fun isGeneratedStub(psiClass: PsiClass): Boolean =
        psiClass.qualifiedName?.startsWith("${MetaClassGenerationSettings.PACKAGE_NAME}.") == true &&
            psiClass.name?.endsWith(MetaClassGenerationSettings.CLASS_NAME_POSTFIX) == true

    fun isGeneratedStubType(type: PsiType?): Boolean {
        val psiClass = type?.let(PsiTypesUtil::getPsiClass) ?: return false
        return isGeneratedStub(psiClass)
    }
}
