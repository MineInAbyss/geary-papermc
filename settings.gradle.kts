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
    val miaLibs: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("miaLibs") {
            from("com.mineinabyss:catalog:$miaLibs")
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
