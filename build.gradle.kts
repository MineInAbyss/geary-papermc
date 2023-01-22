val idofrontVersion: String by project

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mia.autoversion)
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
        maven("https://jitpack.io")
    }
}
