plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqlite.kt)
}

dependencies {
    api(libs.sqlite.kt)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)
}


sqliteKt {
    register("spawns") {
        mainClassName = "SpawnsDatabase"
        packageName = "com.mineinabyss.geary.papermc.data"
    }
}