plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.nms.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
}

dependencies {
    compileOnly(miaLibs.idofront.nms)
    compileOnly(miaLibs.idofront.services)
    compileOnly(miaLibs.minecraft.mccoroutine)
    implementation(libs.geary.uuid)
    api(project(":geary-papermc-datastore"))
    api(project(":geary-papermc-core"))
}
