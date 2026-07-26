plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    id(miaLibs.plugins.mia.nms.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-papermc-tracking"))
    implementation(project(":geary-papermc-spawning"))
    implementation(libs.geary.serialization)
    implementation(libs.geary.autoscan)
    implementation(libs.geary.actions)

    // MineInAbyss platform
    compileOnly(miaLibs.kotlin.stdlib)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)
    compileOnly(miaLibs.idofront.nms)
    compileOnly(miaLibs.creative.serializer.minecraft)
    compileOnly(miaLibs.creative.api)
}
