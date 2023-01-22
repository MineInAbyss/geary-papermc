@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.asProvider().get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-papermc-core"))
    api(myLibs.geary.prefabs)
    api(myLibs.geary.serialization)

    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.cbor)
}
