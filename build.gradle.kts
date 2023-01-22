import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val idofrontVersion: String by project

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mia.autoversion)
}

allprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
        maven("https://jitpack.io")
    }

    dependencies {
        val libs = rootProject.libs
        implementation(libs.bundles.idofront.core)
    }

    tasks {
        withType<KotlinCompile>() {
            kotlinOptions {
                freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
            }
        }
    }
}
