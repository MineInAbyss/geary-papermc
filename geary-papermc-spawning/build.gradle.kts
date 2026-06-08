plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.nms.get().pluginId)
    id(miaLibs.plugins.mia.testing.get().pluginId)
    alias(miaLibs.plugins.mia.publication)
    alias(miaLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.sqlite.kt)
    // Plugins
    compileOnly(miaLibs.minecraft.plugin.mythic.dist)
    compileOnly(miaLibs.idofront.nms)
    compileOnly(miaLibs.idofront.util)

    // Other deps
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.minecraft.mccoroutine)
    testImplementation(miaLibs.mockk)

    compileOnly(libs.geary.actions)
    implementation(projects.gearyPapermcSqlite)
    implementation(projects.gearyPapermcTracking)
    testImplementation(miaLibs.kotlinx.coroutines.test)
//    compileOnly(miaLibs.kotlinx.dataframe)
//    implementation(project(":geary-papermc-features"))
}

/*configurations.all {
    resolutionStrategy.cacheChangingModulesFor( 0, "seconds")
}*/
