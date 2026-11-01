plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.testing.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":"))

    testImplementation(libs.geary.test)
    testImplementation(miaLibs.idofront.di)
    testImplementation(miaLibs.idofront.serializers)
    testImplementation(miaLibs.idofront.services)
    testImplementation(miaLibs.minecraft.mccoroutine.core)
    testImplementation(miaLibs.minecraft.mockbukkit)
    testImplementation(miaLibs.minecraft.papermc)
    testImplementation(miaLibs.logback.classic)
    testImplementation(miaLibs.kotlinx.serialization.kaml)
    testImplementation(miaLibs.creative.api)
    testImplementation(miaLibs.creative.serializer.minecraft)
    testImplementation(libs.bytebuddy)
}

// Reuses the test dependencies for the plain Paper API, without the test classes so test components stay out of the schema
val schemas by sourceSets.creating {
    compileClasspath += sourceSets.test.get().compileClasspath - sourceSets.test.get().output
    runtimeClasspath += output + sourceSets.test.get().runtimeClasspath - sourceSets.test.get().output
}

dependencies {
    "schemasImplementation"(project(":geary-papermc-schemas"))
    "schemasImplementation"(project(":geary-papermc-nexo"))
    "schemasImplementation"(project(":geary-papermc-menus"))
    "schemasImplementation"(project(":geary-papermc-creativetabs"))
}

tasks.register<JavaExec>("generateSchemas") {
    group = "documentation"
    description = "Writes JSON schemas for configs and prefabs, pass -PschemaOutput=<server-config> to write into a server config repo"
    classpath = schemas.runtimeClasspath
    mainClass = "SchemasKt"
    args(providers.gradleProperty("schemaOutput").orElse(layout.buildDirectory.dir("schemas").map { it.asFile.path }).get())
}
