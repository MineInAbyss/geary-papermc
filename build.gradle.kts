plugins {
    `java-library`
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.mia.autoversion)
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
    repositories {
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://maven.enginehub.org/repo/") //WorldGuard/Edit
        maven("https://mvn.lumine.io/repository/maven-public/") // Mythic
        maven("https://repo.nexomc.com/releases")
        mavenLocal()
    }


    if (project.name != "schema-generator") {
        apply(plugin = "kotlin")

        dependencies {
            compileOnly(rootProject.idofrontLibs.bundles.idofront.core)
            testImplementation(rootProject.idofrontLibs.bundles.idofront.core)
        }
        kotlin {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                    "-opt-in=kotlin.ExperimentalUnsignedTypes",
                    "-Xcontext-parameters",
                )
                optIn.addAll(
                    "kotlin.time.ExperimentalTime"
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
