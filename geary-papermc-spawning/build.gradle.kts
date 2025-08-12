plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.testing.get().pluginId)
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.sqlite.kt)
    // Plugins
    compileOnly(idofrontLibs.minecraft.plugin.mythic.dist)
    compileOnly(idofrontLibs.idofront.nms)
    compileOnly(idofrontLibs.idofront.util)

    // Other deps
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
    testImplementation(idofrontLibs.mockk)

    compileOnly(libs.geary.actions)
    implementation(projects.gearyPapermcSqlite)
    implementation(projects.gearyPapermcTracking)
    compileOnly(idofrontLibs.minecraft.plugin.worldguard) {
        exclude(group = "org.bukkit")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
    }
    testImplementation(idofrontLibs.kotlinx.coroutines.test)
//    compileOnly(idofrontLibs.kotlinx.dataframe)
//    implementation(project(":geary-papermc-features"))
}

/*configurations.all {
    resolutionStrategy.cacheChangingModulesFor( 0, "seconds")
}*/
