plugins {
    id(idofrontLibs.plugins.mia.copyjar.get().pluginId)
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.snakeyaml")
        exclude(group = "it.unimi.dsi")
    }
}

dependencies {
    implementation(project(":"))
    compileOnly("dev.jorel:commandapi-bukkit-core:9.3.0")
    compileOnly("dev.jorel:commandapi-bukkit-kotlin:9.3.0")
    implementation("dev.jorel:commandapi-bukkit-shade:9.3.0")

    // MineInAbyss platform
    compileOnly(idofrontLibs.kotlin.stdlib)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
}

copyJar {
    jarName.set("geary-$version.jar")
}
