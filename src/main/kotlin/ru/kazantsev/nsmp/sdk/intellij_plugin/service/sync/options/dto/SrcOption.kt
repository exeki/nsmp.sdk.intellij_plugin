package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.dto

import kotlinx.serialization.Serializable

@Serializable
class SrcOption (
    val code : String,
    val title : String
)