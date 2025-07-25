plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

repositories {
    mavenLocal()
}

dependencies {
    implementation("me.dvyy:sqlite-kt:0.0.2-alpha.1")
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
    implementation(project(":geary-papermc-tracking"))
    compileOnly(idofrontLibs.minecraft.plugin.worldguard) {
        exclude(group = "org.bukkit")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
    }
//    compileOnly(idofrontLibs.kotlinx.dataframe)
//    implementation(project(":geary-papermc-features"))
}

/*configurations.all {
    resolutionStrategy.cacheChangingModulesFor( 0, "seconds")
}*/
