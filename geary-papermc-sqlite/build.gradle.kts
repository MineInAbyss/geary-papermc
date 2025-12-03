plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
    id("com.mineinabyss.sqlitekt.codegen")
}

dependencies {
    api(libs.sqlite.kt)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
}


sqliteCodegen {
    register("spawns") {
        packageName = "com.mineinabyss.geary.papermc.data"
    }
}