plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    alias(miaLibs.plugins.compose.compiler)
}

dependencies {
    compileOnly(miaLibs.guiy)
    compileOnly(miaLibs.kotlinx.coroutines)

    implementation(project(":geary-papermc-tracking"))
    implementation(project(":geary-papermc-nexo"))
}
