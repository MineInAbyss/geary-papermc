plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    compileOnly(idofrontLibs.minecraft.plugin.mythic.dist)
    compileOnly(idofrontLibs.idofront.nms)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
    implementation(gearyLibs.uuid)
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))
}
