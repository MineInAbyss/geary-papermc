rootProject.name = "geary-papermc"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        google()
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
            version("mockbukkit", "4.108.0")
            version("minecraft", "1.21.11-R0.1-SNAPSHOT")
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
    "geary-papermc-sqlite",
    "geary-tests",
//    "schema-generator"
)

includeBuild("../features")