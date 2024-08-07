plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-papermc-core"))
    api(libs.geary.prefabs)
    api(libs.geary.serialization)

    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.cbor)
}
