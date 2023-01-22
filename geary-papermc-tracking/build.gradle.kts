@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.asProvider().get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
}

dependencies {
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))
}
