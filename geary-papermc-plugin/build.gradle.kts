@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.copyjar.get().pluginId)
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    id(libs.plugins.mia.nms.reobfuscate.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.snakeyaml")
        exclude(group = "com.charleskorn.kaml")
    }
}

dependencies {
    implementation(project(":geary-papermc-tracking"))
    implementation(gearyLibs.autoscan)
    implementation(gearyLibs.prefabs)
    implementation(gearyLibs.serialization)
    implementation(gearyLibs.uuid)

    // Plugins
    compileOnly(myLibs.plugman)

    // MineInAbyss platform
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)
}

copyJar {
    jarName.set("geary-$version.jar")
}
