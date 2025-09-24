package com.mineinabyss.geary.papermc.features.common.conditions.location

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag

//TODO probably useful in idofront?
/**
 * Serializable class for matching materials and tags, uses a string list where tags start with #
 */
@Serializable(with = MaterialOrTagMatcher.Serializer::class)
data class MaterialOrTagMatcher(
    val materials: List<Material>,
    val tags: List<Tag<Material>>,
) {
    fun matches(material: Material): Boolean {
        return (materials.isEmpty() && tags.isEmpty()) || !notMatches(material)
    }

    fun notMatches(material: Material) = (materials.isEmpty() || material !in materials) && (tags.isEmpty() || tags.none { it.isTagged(material) })

    object Serializer : InnerSerializer<List<String>, MaterialOrTagMatcher>(
        "MaterialOrTagMatcher",
        ListSerializer(String.Companion.serializer()),
        { Serializer.decodeList(it) },
        { Serializer.encodeList(it) }
    ) {
        fun encodeList(matcher: MaterialOrTagMatcher): List<String> {
            return matcher.materials.map { it.key.toString() } + matcher.tags.map { "#" + it.key.asMinimalString() }
        }

        fun decodeList(list: List<String>): MaterialOrTagMatcher {
            val (tags, materials) = list.partition { it.startsWith("#") }
            return MaterialOrTagMatcher(
                materials.mapNotNull { Material.getMaterial(it.uppercase()) },
                tags.mapNotNull { tag ->
                    Bukkit.getTag(
                        Tag.REGISTRY_BLOCKS,
                        NamespacedKey.fromString(tag.removePrefix("#")) ?: return@mapNotNull null.also { Geary.w { "Invalid tag: $tag" } },
                        Material::class.java
                    ) ?: null.also { Geary.w { "Unknown block tag: $tag" } }
                }
            )
        }
    }
}
