plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-papermc-core"))
    api(libs.geary.prefabs)
    api(libs.geary.serialization)

    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.cbor)
}
