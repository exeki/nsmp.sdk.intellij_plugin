package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.model

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto

data class MetaClassListItem(
    val fullCode: String,
    val title: String,
) {
    override fun toString(): String = "$title ($fullCode)"

    companion object {
        fun fromDto(dto: MetaClassDto): MetaClassListItem =
            MetaClassListItem(fullCode = dto.fullCode, title = dto.title)
    }
}
