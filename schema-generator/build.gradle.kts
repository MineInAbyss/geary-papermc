plugins {
    id(miaLibs.plugins.kotlin.multiplatform.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(npm("ts-json-schema-generator", "2.3.0"))
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
            }
        }

        jvmMain {
            dependencies {
                implementation(project(":"))
                implementation(libs.kts.to.typescript)
                implementation(miaLibs.bundles.idofront.core)
                implementation(miaLibs.kotlin.reflect)
                implementation(miaLibs.kotlinx.serialization.kaml)
            }
        }
    }
}
