plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
}

// Plugins on older catalogs still target Java 21 and pull this into their schema tasks
idofront {
    jvmVersion.set(21)
}

dependencies {
    api(libs.jsonschema.writer)
    implementation(miaLibs.reflections)
    compileOnly(libs.geary.actions)
    compileOnly(libs.geary.autoscan)
    compileOnly(libs.geary.serialization)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
}
