import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.copyjar.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.nms.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.snakeyaml")
        exclude(group = "it.unimi.dsi")
        exclude(group = "io.insert-koin")
        exclude(group = "com.mineinabyss.dependencies")
        exclude(group = "org.kodein.di")
    }
}

paperweight.reobfArtifactConfiguration.set(ReobfArtifactConfiguration.MOJANG_PRODUCTION)

dependencies {
    implementation(project(":"))

    // MineInAbyss platform
    compileOnly(miaLibs.kotlin.stdlib)
    compileOnly(miaLibs.idofront.services)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)
}

copyJar {
    jarName.set("geary-$version.jar")
}

paper {
    name = "Geary"
    main = "com.mineinabyss.geary.papermc.plugin.GearyPluginImpl"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    description = "An entity component system written for PaperMC"

    serverDependencies {
        register("Idofront") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("MythicMobs") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("WorldGuard") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
    }
}