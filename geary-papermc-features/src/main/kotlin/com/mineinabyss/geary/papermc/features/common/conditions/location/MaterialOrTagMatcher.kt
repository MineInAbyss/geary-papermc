package com.mineinabyss.geary.papermc.features.common.conditions.location

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
    fun matches(material: Material) =
        (materials.isEmpty() || material in materials) && (tags.isEmpty() || tags.any { it.isTagged(material) })

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
                tags.mapNotNull {
                    Bukkit.getTag<Material>(
                        Tag.REGISTRY_BLOCKS,
                        NamespacedKey.fromString(it.removePrefix("#")) ?: return@mapNotNull null,
                        Material::class.java
                    )
                }
            )
        }
    }
}
