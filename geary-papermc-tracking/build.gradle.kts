@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    id(libs.plugins.mia.nms.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
}

dependencies {
    compileOnly(gearyLibs.uuid)
    compileOnly(libs.idofront.nms)
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))
}
