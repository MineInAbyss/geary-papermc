import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val idofrontVersion: String by project

plugins {
    java
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.dokka) apply false
    alias(idofrontLibs.plugins.mia.autoversion)
}

dependencies {
    api(project(":geary-papermc-tracking"))
    api(project(":geary-papermc-bridge"))
    api(gearyLibs.core)
    api(gearyLibs.autoscan)
    api(gearyLibs.prefabs)
    api(gearyLibs.serialization)
    api(gearyLibs.uuid)
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        mavenLocal()
    }

    dependencies {
        val libs = rootProject.idofrontLibs
        compileOnly(libs.bundles.idofront.core)
        testImplementation(libs.bundles.idofront.core)
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs += listOf(
                    "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                    "-opt-in=kotlin.ExperimentalUnsignedTypes",
                )
            }
        }
    }
}

// Build server jar when used as a composite project
tasks {
    build {
        dependsOn(project(":geary-papermc-plugin").tasks.build)
    }
}
