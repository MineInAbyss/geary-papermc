@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.asProvider().get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))

    implementation(project(":geary-papermc-core"))

    // MineInAbyss platform
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)

    implementation(libs.bundles.idofront.core)
}
