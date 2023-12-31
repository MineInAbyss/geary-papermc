package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import org.bukkit.inventory.ItemStack
import java.util.*

class SetItemMigrationSystem : GearyListener() {
    private val Pointers.setItem by get<SetItem>().whenSetOnTarget()
    private val Pointers.item by get<ItemStack>().whenSetOnTarget()
    private val Pointers.overrides by get<SetItemIgnoredProperties>().orNull().on(target)
    override fun Pointers.handle() {
        val ignoredProperties =
            overrides?.ignoreAsEnumSet() ?: EnumSet.noneOf(BaseSerializableItemStack.Properties::class.java)
        setItem.item.toItemStack(applyTo = item, ignoredProperties)
    }
}
