@Suppress("DSL_SCOPE_VIOLATION")
plugins {
//    id(libs.plugins.mia.copyjar.get().pluginId)
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
//    id(libs.plugins.mia.papermc.get().pluginId)
//    id(libs.plugins.mia.nms.get().pluginId)
    id(libs.plugins.mia.testing.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}
dependencies {
    implementation(project(":"))

    testImplementation(libs.minecraft.mockbukkit)
//    testImplementation(libs.minecraft.papermc)

}
