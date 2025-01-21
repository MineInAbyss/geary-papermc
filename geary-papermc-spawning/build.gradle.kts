plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    // Plugins
    compileOnly(idofrontLibs.minecraft.plugin.mythic.dist)
    compileOnly(idofrontLibs.idofront.nms)
    compileOnly(idofrontLibs.idofront.util)

    // Other deps
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.minecraft.mccoroutine)

    compileOnly(libs.geary.actions)
    implementation(project(":geary-papermc-tracking"))
    compileOnly(idofrontLibs.minecraft.plugin.worldguard) { exclude(group = "org.bukkit") }
    compileOnly(idofrontLibs.kotlinx.dataframe)
//    implementation(project(":geary-papermc-features"))
}

/*configurations.all {
    resolutionStrategy.cacheChangingModulesFor( 0, "seconds")
}*/
