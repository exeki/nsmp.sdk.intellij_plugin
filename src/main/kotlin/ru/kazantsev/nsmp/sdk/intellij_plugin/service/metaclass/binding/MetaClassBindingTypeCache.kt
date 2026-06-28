package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.components.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Кэширует привязку переменная → metaCode в Groovy-файле.
 * [PsiType] каждый раз создаётся заново из актуальной заглушки, чтобы не залипать на устаревшем PSI.
 */
@Service(Service.Level.PROJECT)
class MetaClassBindingTypeCache {

    data class VariableKey(val filePath: String, val variableName: String)

    private val metaCodeByVariable = ConcurrentHashMap<VariableKey, String>()

    fun rememberMetaCode(key: VariableKey, metaCode: String) {
        metaCodeByVariable[key] = metaCode
    }

    fun getRememberedMetaCode(key: VariableKey): String? = metaCodeByVariable[key]

    fun clearForFile(filePath: String) {
        metaCodeByVariable.keys.removeIf { it.filePath == filePath }
    }

    fun clear() {
        metaCodeByVariable.clear()
    }
}
