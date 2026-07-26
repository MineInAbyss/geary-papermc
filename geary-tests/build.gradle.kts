plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.testing.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":"))

    testImplementation(libs.geary.test)
    testImplementation(miaLibs.idofront.di)
    testImplementation(miaLibs.idofront.serializers)
    testImplementation(miaLibs.idofront.services)
    testImplementation(miaLibs.minecraft.mccoroutine.core)
    testImplementation(miaLibs.minecraft.mockbukkit)
    testImplementation(miaLibs.minecraft.papermc)
    testImplementation(miaLibs.logback.classic)
    testImplementation(miaLibs.kotlinx.serialization.kaml)
    testImplementation(libs.bytebuddy)
}
