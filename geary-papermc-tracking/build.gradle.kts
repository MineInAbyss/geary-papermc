plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    id(libs.plugins.mia.nms.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    compileOnly(libs.minecraft.plugin.mythic.dist)
    compileOnly(libs.idofront.nms)
    compileOnly(libs.minecraft.mccoroutine)
    implementation(gearyLibs.uuid)
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))
}
