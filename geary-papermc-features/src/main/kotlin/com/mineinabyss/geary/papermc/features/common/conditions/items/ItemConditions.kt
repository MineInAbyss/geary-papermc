package com.mineinabyss.geary.papermc.features.common.conditions.items

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.item
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.FloatRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.SingleOrListSerializer
import com.mineinabyss.idofront.serialization.StringsSerializer
import com.mineinabyss.idofront.serialization.TriStateSerializer
import com.mineinabyss.idofront.util.FloatRange
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import net.kyori.adventure.util.TriState
import org.bukkit.Color

@Serializable
@SerialName("geary:check.item")
class ItemConditions(
    val matches: SerializableItemStack? = null,
    val prefabs: List<PrefabKey> = listOf(),
    val customModelData: CustomModelDataConditions? = null,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val item = item ?: return false

        if (matches != null && !matches.matches(item)) return false

        if (prefabs.isNotEmpty()) {
            val itemPrefabs = entity?.prefabs ?: return false
            if (itemPrefabs.none { it.get<PrefabKey>() in prefabs }) return false
        }

        if (customModelData != null) {
            val data = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA) ?: return false
            if (!customModelData.matches(data)) return false
        }

        return true
    }
}

@Serializable
data class CustomModelDataConditions(
    val strings: @Serializable(StringsSerializer::class) List<String> = listOf(),
    val flags: @Serializable(FlagsSerializer::class) List<TriState> = listOf(),
    val floats: @Serializable(FloatsSerializer::class) List<FloatRange?> = listOf(),
    val colors: @Serializable(ColorsSerializer::class) List<Color?> = listOf(),
) {
    fun matches(data: CustomModelData): Boolean {
        if (!data.strings().containsAll(strings)) return false
        if (!flags.map { it.toBoolean() }.matchesByIndex(data.flags()) { item, flag -> item == flag }) return false
        if (!floats.matchesByIndex(data.floats()) { item, range -> item in range }) return false
        if (!colors.matchesByIndex(data.colors()) { item, color -> item == color }) return false
        return true
    }

    private inline fun <T, C> List<T?>.matchesByIndex(itemValues: List<C>, matches: (C, T) -> Boolean): Boolean =
        withIndex().all { (index, expected) ->
            expected == null || itemValues.getOrNull(index)?.let { matches(it, expected) } == true
        }
}

object FlagsSerializer : KSerializer<List<TriState>> by SingleOrListSerializer(TriStateSerializer)
object FloatsSerializer : KSerializer<List<FloatRange?>> by SingleOrListSerializer(FloatRangeSerializer.nullable)
object ColorsSerializer : KSerializer<List<Color?>> by SingleOrListSerializer(ColorSerializer.nullable)
