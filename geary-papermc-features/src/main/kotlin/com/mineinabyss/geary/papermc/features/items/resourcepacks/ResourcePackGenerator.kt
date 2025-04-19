package com.mineinabyss.geary.papermc.features.items.resourcepacks

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.resourcepacks.ResourcePacks
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemType
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.item.Item
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures

class ResourcePackGenerator(world: Geary) : Geary by world {
    private val resourcePackQuery = cache(::ResourcePackQuery)
    private val includedPackPath = gearyPaper.config.resourcePack.includedPackPath.takeUnless(String::isEmpty)
        ?.let { gearyPaper.plugin.dataFolder.resolve(it) }
    private val resourcePack = includedPackPath?.let(ResourcePacks::readToResourcePack) ?: ResourcePack.resourcePack()

    fun generateResourcePack() {
        if (!gearyPaper.config.resourcePack.generate) return
        val resourcePackFile = gearyPaper.plugin.dataFolder.resolve(gearyPaper.config.resourcePack.outputPath)
        resourcePackFile.deleteRecursively()

        resourcePackQuery.forEach { (prefabKey, resourcePackContent, itemStack) ->
            val vanillaModelKey = ResourcePacks.vanillaKeyForMaterial(
                resourcePackContent.baseMaterial ?: itemStack?.type
                ?: return@forEach logger.w("$prefabKey has no type/baseMaterial defined in either ResourcePackContent or SerializableItemStack")
            )
            val defaultVanillaModel = ((resourcePack.model(vanillaModelKey)
                ?: ResourcePacks.vanillaResourcePack.model(vanillaModelKey)))
                ?.toBuilder() ?: Model.model().key(vanillaModelKey)

            // Generates any missing models for predicates if only textures are provided
            generatePredicateModels(resourcePack, resourcePackContent, prefabKey)

            // If a model is defined we assume it exists in the resourcepack already, and just add the override to the vanilla model
            if (resourcePackContent.model != null) {
                resourcePackContent.itemOverrides(resourcePackContent.model.key(), prefabKey, itemStack)
                    .forEach(defaultVanillaModel::addOverride)
            } else { // If it only has textures we need to generate the model ourselves and add it
                val model = Model.model()
                    .key(Key.key(prefabKey.namespace, prefabKey.key))
                    .parent(resourcePackContent.parentModel.key())
                    .textures(resourcePackContent.textures.modelTextures).build()
                resourcePackContent.itemOverrides(model.key(), prefabKey, itemStack)
                    .forEach(defaultVanillaModel::addOverride)
                model
                    .let(ResourcePacks::ensureItemOverridesSorted)
                    .addTo(resourcePack)

                val itemKey = itemStack?.getData(DataComponentTypes.ITEM_MODEL)
                    ?.takeUnless { itemStack.type.asItemType()?.getDefaultData(DataComponentTypes.ITEM_MODEL) == it }
                    ?: Key.key(prefabKey.full)
                val item = Item.item(itemKey, ItemModel.reference(model.key()))
                if (resourcePack.item(itemKey) == null) resourcePack.item(item)
            }

            defaultVanillaModel.build()
                .let(ResourcePacks::ensureItemOverridesSorted)
                .addTo(resourcePack)
        }

        ResourcePacks.writeToFile(resourcePackFile, resourcePack)
    }

    private fun generatePredicateModels(
        resourcePack: ResourcePack,
        resourcePackContent: ResourcePackContent,
        prefabKey: PrefabKey,
    ) {
        fun predicateModel(modelKey: Key, suffix: String) {
            Model.model().key(Key.key(prefabKey.namespace, prefabKey.key.plus(suffix)))
                .parent(resourcePackContent.parentModel)
                .textures(ModelTextures.of(listOf(ModelTexture.ofKey(modelKey)), null, emptyMap()))
                .build().addTo(resourcePack)
        }

        val predicates = resourcePackContent.itemPredicates
        predicates.blockingTexture?.let { predicateModel(it, "_blocking") }
        predicates.brokenTexture?.let { predicateModel(it, "_broken") }
        predicates.castTexture?.let { predicateModel(it, "_cast") }
        predicates.chargedTexture?.let { predicateModel(it, "_charged") }
        predicates.fireworkTexture?.let { predicateModel(it, "_firework") }
        predicates.lefthandedTexture?.let { predicateModel(it, "_lefthanded") }
        predicates.throwingTexture?.let { predicateModel(it, "_throwing") }
        predicates.angleTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_angle_$i") }
        predicates.cooldownTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_cooldown_$i") }
        predicates.damageTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_damage_$i") }
        predicates.pullingTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_pulling_$i") }
        predicates.timeTextures.onEachIndexed { i, (key, _) -> predicateModel(key, "_time_$i") }

    }

    companion object {
        class ResourcePackQuery(world: Geary) : GearyQuery(world) {
            private val prefabKey by get<PrefabKey>()
            private val resourcePackContent by get<ResourcePackContent>()
            //private val itemstack by get<SerializableItemStack>().orNull()

            override fun ensure() = this {
                has<Prefab>()
            }

            operator fun component1() = prefabKey
            operator fun component2() = resourcePackContent
            operator fun component3() = world.getAddon(ItemTracking).itemProvider.serializePrefabToItemStack(prefabKey)
        }
    }
}
