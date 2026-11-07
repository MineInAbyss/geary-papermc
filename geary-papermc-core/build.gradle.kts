plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.nms.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

// Resolved on its own because cartridge-api and paper-api declare the same capability
val cartridgeApi: Configuration by configurations.creating { isTransitive = false }

dependencies {
    cartridgeApi("com.mineinabyss.cartridge:cartridge-api:26.3-R0.1-SNAPSHOT")
    api(libs.geary.core)
    api(libs.geary.serialization)
    api(libs.geary.prefabs)
    compileOnly(libs.geary.actions)

    // MineInAbyss platform
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.minecraft.mccoroutine)
    compileOnly(miaLibs.bundles.idofront.core)
    compileOnly(files(cartridgeApi))
}
