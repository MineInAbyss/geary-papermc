package com.mineinabyss.geary.papermc.features.items.resourcepacks

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.resourcepacks.ResourcePacks
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures

class ResourcePackGenerator {

    private val resourcePack: ResourcePack = ResourcePack.resourcePack()
    private val resourcePackQuery = geary.cache(ResourcePackQuery())

    fun generateResourcePack() {
        if (!gearyPaper.config.resourcePack.generate) return
        val resourcePackFile = gearyPaper.plugin.dataFolder.resolve(gearyPaper.config.resourcePack.outputPath)
        resourcePackFile.deleteRecursively()

        resourcePackQuery.forEach { (prefabKey, resourcePackContent) ->
            val defaultVanillaModel = Key.key("item/${resourcePackContent.baseMaterial.key().value()}").let { resourcePack.model(it)?.toBuilder() ?: Model.model().key(it) }

            // Generates any missing models for predicates if only textures are provided
            generatePredicateModels(resourcePack, resourcePackContent, prefabKey)

            // If a model is defined we assume it exists in the resourcepack already, and just add the override to the vanilla model
            if (resourcePackContent.model != null) {
                resourcePackContent.itemOverrides(resourcePackContent.model.key()).forEach(defaultVanillaModel::addOverride)
            } else { // If it only has textures we need to generate the model ourselves and add it
                val model = Model.model()
                    .key(Key.key(prefabKey.namespace, prefabKey.key))
                    .parent(resourcePackContent.parentModel.key())
                    .textures(resourcePackContent.textures.modelTextures).build()
                resourcePackContent.itemOverrides(model.key()).forEach(defaultVanillaModel::addOverride)
                model.addTo(resourcePack)
            }

            defaultVanillaModel.build().addTo(resourcePack)
        }

        when {
            resourcePackFile.extension == "zip" -> ResourcePacks.resourcePackWriter.writeToZipFile(resourcePackFile, resourcePack)
            resourcePackFile.isDirectory -> ResourcePacks.resourcePackWriter.writeToDirectory(resourcePackFile, resourcePack)
            else -> {
                gearyPaper.logger.w("Failed to generate resourcepack in ${resourcePackFile.path}")
                gearyPaper.logger.w("Outputting to default plugins/Geary/resourcepack directory")
                ResourcePacks.resourcePackWriter.writeToDirectory(gearyPaper.plugin.dataFolder.resolve("resourcepack"), resourcePack)
            }
        }
    }

    private fun generatePredicateModels(resourcePack: ResourcePack, resourcePackContent: ResourcePackContent, prefabKey: PrefabKey) {
        fun predicateModel(modelKey: Key, suffix: String) {
            Model.model().key(Key.key(prefabKey.namespace, prefabKey.key.plus(suffix)))
                .parent(resourcePackContent.parentModel)
                .textures(ModelTextures.of(listOf(ModelTexture.ofKey(modelKey)), null, emptyMap()))
                .build().addTo(resourcePack)
        }
        resourcePackContent.itemPredicates.blockingTexture?.let { predicateModel(it, "_blocking") }
        resourcePackContent.itemPredicates.brokenTexture?.let { predicateModel(it, "_broken") }
        resourcePackContent.itemPredicates.castTexture?.let { predicateModel(it, "_cast") }
        resourcePackContent.itemPredicates.chargedTexture?.let { predicateModel(it, "_charged") }
        resourcePackContent.itemPredicates.fireworkTexture?.let { predicateModel(it, "_firework") }
        resourcePackContent.itemPredicates.lefthandedTexture?.let { predicateModel(it, "_lefthanded") }
        resourcePackContent.itemPredicates.throwingTexture?.let { predicateModel(it, "_throwing") }
        resourcePackContent.itemPredicates.angleTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_angle_$i") }
        resourcePackContent.itemPredicates.cooldownTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_cooldown_$i") }
        resourcePackContent.itemPredicates.damageTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_damage_$i") }
        resourcePackContent.itemPredicates.pullingTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_pulling_$i") }
        resourcePackContent.itemPredicates.timeTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_time_$i") }

    }

    companion object {
        class ResourcePackQuery : GearyQuery() {
            private val prefabKey by get<PrefabKey>()
            private val resourcePackContent by get<ResourcePackContent>()

            override fun ensure() = this {
                has<Prefab>()
            }

            operator fun component1() = prefabKey
            operator fun component2() = resourcePackContent
        }
    }
}