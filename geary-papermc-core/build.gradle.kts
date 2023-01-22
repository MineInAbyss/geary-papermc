@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.asProvider().get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    api(myLibs.geary.core)

    // MineInAbyss platform
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
}
