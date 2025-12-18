plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.testing.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":"))

    testImplementation(libs.geary.test)
    testImplementation(idofrontLibs.idofront.features)
    testImplementation(idofrontLibs.idofront.serializers)
    testImplementation(idofrontLibs.idofront.services)
    testImplementation(idofrontLibs.minecraft.mccoroutine.core)
    testImplementation(idofrontLibs.minecraft.mockbukkit)
    testImplementation(idofrontLibs.minecraft.papermc)
    testImplementation(idofrontLibs.logback.classic)
    testImplementation(idofrontLibs.kotlinx.serialization.kaml)
    testImplementation(libs.bytebuddy)
}
