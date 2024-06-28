rootProject.name = "geary-papermc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenLocal()
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings
    val gearyVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("idofrontLibs").from("com.mineinabyss:catalog:$idofrontVersion")
        create("gearyLibs").from("com.mineinabyss:geary-catalog:$gearyVersion")
    }
}

include(
    "geary-papermc-bridge",
    "geary-papermc-integrations",
    "geary-papermc-core",
    "geary-papermc-datastore",
    "geary-papermc-plugin",
    "geary-papermc-tracking",
    "geary-tests"
)
