plugins {
//    id(idofrontLibs.plugins.mia.copyjar.get().pluginId)
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
//    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
//    id(idofrontLibs.plugins.mia.nms.get().pluginId)
    id(idofrontLibs.plugins.mia.testing.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}
dependencies {
    implementation(project(":"))

    testImplementation(idofrontLibs.minecraft.mockbukkit)
}
