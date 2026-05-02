package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.dto

import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
class SrcOptionRoot (
    val scripts : List<SrcOption>,
    val modules : List<SrcOption>,
    val advImports : List<SrcOption>
)