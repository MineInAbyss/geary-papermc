import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val idofrontVersion: String by project

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.mia.kotlin.jvm)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.mia.autoversion)
}

dependencies {
    api(project(":geary-papermc-tracking"))
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/nms/")
    }

    dependencies {
        val libs = rootProject.libs
        implementation(libs.bundles.idofront.core)
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
