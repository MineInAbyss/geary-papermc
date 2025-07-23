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

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("idofrontLibs") {
            from("com.mineinabyss:catalog:$idofrontVersion")
        }
    }
}


val includeGeary: String? by settings

if(includeGeary.toBoolean()) includeBuild("../geary")

include(
    "geary-papermc-features",
    "geary-papermc-mythicmobs",
    "geary-papermc-core",
    "geary-papermc-datastore",
    "geary-papermc-plugin",
    "geary-papermc-spawning",
    "geary-papermc-tracking",
    "geary-tests",
//    "schema-generator"
)
