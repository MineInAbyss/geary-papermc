package com.mineinabyss.geary.papermc.features.items.resourcepacks

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.prefabs.PrefabsDSLExtensions
import com.mineinabyss.geary.prefabs.PrefabsDSLExtensions.walkJarResources
import team.unnamed.creative.ResourcePack
import kotlin.reflect.KClass

val ResourcePackAddon = createAddon<ResourcePackConfig, ResourcePackGenerator>(
    "Resource Pack Addon",
    configuration = { ResourcePackConfig() }
) {
    val generator = ResourcePackGenerator(geary, configuration)
    on(GearyPhase.ENABLE) {
        generator.generateResourcePack()
    }
    generator
}

data class ResourcePackConfig(
    val jarResources: MutableList<PrefabsDSLExtensions.JarResource> = mutableListOf(),
    val resourcePacks: MutableList<ResourcePack> = mutableListOf()
) {
    fun fromJarResourceDirectory(classLoaderRef: KClass<*>, resource: String) {
        val classLoader = classLoaderRef.java.classLoader
        walkJarResources(classLoader, resource).forEach { jarResources.add(it) }
    }

    fun addResourcePack(resourcePack: ResourcePack) {
        resourcePacks.add(resourcePack)
    }
}

@GearyDSL
fun GearySetup.resourcePack(
    configure: ResourcePackConfig.() -> Unit
) {
    install(ResourcePackAddon) {
        configure()
    }
}