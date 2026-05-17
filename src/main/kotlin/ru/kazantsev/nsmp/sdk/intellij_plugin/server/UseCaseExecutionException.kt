package ru.kazantsev.nsmp.sdk.intellij_plugin.server

class UseCaseExecutionException(val code : Int, override val message : String) : RuntimeException(message)