@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-papermc-core"))
    api(gearyLibs.prefabs)
    api(gearyLibs.serialization)

    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.cbor)
}
