plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

repositories {
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    // Plugins
    compileOnly(idofrontLibs.minecraft.plugin.mythic.dist)
    compileOnly(idofrontLibs.minecraft.plugin.modelengine)
    compileOnly(idofrontLibs.idofront.nms)

    // Other deps
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.minecraft.mccoroutine)

    compileOnly(libs.geary.actions)
    implementation(project(":geary-papermc-tracking"))
}

/*configurations.all {
    resolutionStrategy.cacheChangingModulesFor( 0, "seconds")
}*/
