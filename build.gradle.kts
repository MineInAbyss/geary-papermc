val idofrontVersion: String by project

plugins {
    java
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.dokka) apply false
    alias(idofrontLibs.plugins.mia.autoversion)
    idea
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

dependencies {
    api(project(":geary-papermc-tracking"))
    api(project(":geary-papermc-mythicmobs"))
    api(project(":geary-papermc-features"))
    api(project(":geary-papermc-spawning"))
    api(libs.geary.core)
    api(libs.geary.autoscan)
    api(libs.geary.prefabs)
    api(libs.geary.serialization)
    api(libs.geary.uuid)
    api(libs.geary.actions)
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://maven.enginehub.org/repo/") //WorldGuard/Edit
        maven("https://mvn.lumine.io/repository/maven-public/") // Mythic
        mavenLocal()
    }

    dependencies {
        val libs = rootProject.idofrontLibs
        compileOnly(libs.bundles.idofront.core)
        testImplementation(libs.bundles.idofront.core)
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=kotlin.ExperimentalUnsignedTypes",
            )
        }
    }
}

// Build server jar when used as a composite project
tasks {
    build {
        dependsOn(project(":geary-papermc-plugin").tasks.build)
    }
}
