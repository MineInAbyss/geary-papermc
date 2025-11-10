import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

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

paperweight.reobfArtifactConfiguration.set(ReobfArtifactConfiguration.MOJANG_PRODUCTION)

dependencies {
    implementation(project(":"))
    implementation(libs.kts.to.typescript)

    // MineInAbyss platform
    compileOnly(idofrontLibs.kotlin.stdlib)
    compileOnly(idofrontLibs.idofront.services)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
}

copyJar {
    jarName.set("geary-$version.jar")
}
