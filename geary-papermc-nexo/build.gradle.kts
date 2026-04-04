plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    // Plugins
    compileOnly("com.nexomc:nexo:1.20.1")
    compileOnly(idofrontLibs.idofront.nms)

    // Other deps
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.minecraft.mccoroutine)

    compileOnly(libs.geary.actions)
    implementation(project(":geary-papermc-tracking"))
    implementation(project(":geary-papermc-features"))
}
