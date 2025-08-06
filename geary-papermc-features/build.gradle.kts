plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-papermc-tracking"))
    implementation(project(":geary-papermc-spawning"))
    implementation(libs.geary.serialization)
    implementation(libs.geary.autoscan)
    implementation(libs.geary.actions)

    // MineInAbyss platform
    compileOnly(idofrontLibs.kotlin.stdlib)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
    compileOnly(idofrontLibs.idofront.nms)
    compileOnly(idofrontLibs.creative.serializer.minecraft)
    compileOnly(idofrontLibs.creative.api)
}
