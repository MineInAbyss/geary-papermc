plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xallow-any-scripts-in-source-roots")
    }
}

dependencies {
    api(project(":geary-papermc-features"))
    api(project(":geary-papermc-tracking"))
}
