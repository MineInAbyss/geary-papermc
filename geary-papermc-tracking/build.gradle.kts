@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    id(libs.plugins.mia.nms.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://mvn.lumine.io/repository/maven-public/")
}
dependencies {
    compileOnly(libs.minecraft.plugin.mythic.dist)
    compileOnly(libs.idofront.nms)
    implementation(gearyLibs.uuid)
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))
}
