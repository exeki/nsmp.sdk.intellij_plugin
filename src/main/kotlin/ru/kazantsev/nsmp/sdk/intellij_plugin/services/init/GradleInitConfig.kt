package ru.kazantsev.nsmp.sdk.intellij_plugin.services.init

object GradleInitConfig {
    const val pluginId: String = "nsmp_sdk"
    const val pluginVersion: String = "2.2.0"
    const val pluginRepositoryUrl: String = "https://maven.pkg.github.com/exeki/*"
    const val pluginRepositoryUsernameExpr: String = "System.getenv(\"GITHUB_USERNAME\")"
    const val pluginRepositoryPasswordExpr: String = "System.getenv(\"GITHUB_TOKEN\")"
}
