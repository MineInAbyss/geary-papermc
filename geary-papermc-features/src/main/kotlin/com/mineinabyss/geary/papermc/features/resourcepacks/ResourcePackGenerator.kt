package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.resourcepacks.ResourcePacks
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.item.Item
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures

class ResourcePackGenerator(
    private val geary: Geary,
    private val plugin: Plugin,
    private val config: GearyPaperConfig,
) : AutoCloseable {
    private val resourcePackQuery = geary.cache(::ResourcePackQuery)
    private val includedPackPath = config.resourcePack.includedPackPath.takeUnless(String::isEmpty)
        ?.let { plugin.dataFolder.resolve(it) }
    private val resourcePack = includedPackPath?.let(ResourcePacks::readToResourcePack) ?: ResourcePack.resourcePack()

    fun generateResourcePack() {
        val resourcePackFile = plugin.dataFolder.resolve(config.resourcePack.outputPath)
        resourcePackFile.deleteRecursively()

        resourcePackQuery.forEach { (prefabKey, content, itemStack) ->
            // Generates any missing models for predicates if only textures are provided
            generatePredicateModels(resourcePack, content, prefabKey)

            //FIXME add back
            if (content.model == null /*|| !content.textures.isEmpty*/) {
                val modelKey = content.model ?: Key.key(prefabKey.full)
                resourcePack.model(
                    Model.model()
                        .key(modelKey)
                        .parent(content.parentModel.key())
                        .textures(content.textures.modelTextures).build()
                )
            }

            val itemKey = itemStack?.getData(DataComponentTypes.ITEM_MODEL)
                ?.takeIf { itemStack.isDataOverridden(DataComponentTypes.ITEM_MODEL) }
                ?: content.itemModel ?: Key.key(prefabKey.full)
            val item = Item.item(itemKey, ItemModel.reference(content.model ?: Key.key(prefabKey.namespace, prefabKey.key), content.tintSources))
            if (resourcePack.item(itemKey) == null) resourcePack.item(item)
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

    override fun close() {
        resourcePackQuery.close()
    }

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
