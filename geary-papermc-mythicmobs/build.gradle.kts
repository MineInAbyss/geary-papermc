plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.nms.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

dependencies {
    // Plugins
    compileOnly(miaLibs.minecraft.plugin.mythic.dist)
    compileOnly(miaLibs.minecraft.plugin.modelengine)
    compileOnly(miaLibs.idofront.nms)

    // Other deps
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.minecraft.mccoroutine)

    compileOnly(libs.geary.actions)
    implementation(project(":geary-papermc-tracking"))
    implementation(project(":geary-papermc-features"))
}
