package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.serialization.SerializableItemStack
import org.bukkit.inventory.ItemStack
import java.util.*

class SetItemMigrationSystem : GearyListener() {
    private val TargetScope.setItem by onSet<SetItem>()
    private val TargetScope.item by onSet<ItemStack>()
    private val TargetScope.overrides by getOrNull<SetItemIgnoredProperties>()

    @Handler
    fun TargetScope.handle() {
        val ignoredProperties = overrides?.ignoreAsEnumSet() ?: EnumSet.noneOf(SerializableItemStack.Properties::class.java)
        setItem.item.toItemStack(applyTo = item, ignoredProperties)
    }
}
