plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

dependencies {
    // Plugins
    compileOnly(miaLibs.minecraft.plugin.nexo)

    // Other deps
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.minecraft.mccoroutine)

    implementation(project(":geary-papermc-tracking"))
}
