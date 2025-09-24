plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
}

dependencies {
    implementation(project(":"))
    implementation(idofrontLibs.bundles.idofront.core)
    implementation(idofrontLibs.minecraft.mockbukkit)
    implementation("com.mineinabyss:idofront-jsonschema")
    implementation("io.github.smiley4:schema-kenerator-serialization:2.4.0")
    implementation("io.github.smiley4:schema-kenerator-core:2.4.0")
    implementation("io.github.smiley4:schema-kenerator-jsonschema:2.4.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters", "-Xcontext-sensitive-resolution")
    }
}