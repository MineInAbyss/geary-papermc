plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    api(libs.sqlite.kt)
    api(idofrontLibs.kotlinx.serialization.json)
    api(idofrontLibs.kotlinx.coroutines)
    api(idofrontLibs.minecraft.mccoroutine)
}
