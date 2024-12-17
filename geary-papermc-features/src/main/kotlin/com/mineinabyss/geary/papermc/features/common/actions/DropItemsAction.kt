@file:UseSerializers(ColorSerializer::class, IntRangeSerializer::class, DoubleRangeSerializer::class)

package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = DropItemsAction.Serializer::class)
class DropItemsAction(
    val items: List<SerializableItemStack>,
) : Action {
    override fun ActionGroupContext.execute() {
        val location = location ?: return
        items.forEach { stack ->
            location.world.dropItemNaturally(location, stack.toItemStack())
        }
    }

    object Serializer : InnerSerializer<List<SerializableItemStack>, DropItemsAction>(
        "geary:drop_items",
        ListSerializer(SerializableItemStackSerializer()),
        { DropItemsAction(it) },
        DropItemsAction::items,
    )
}
