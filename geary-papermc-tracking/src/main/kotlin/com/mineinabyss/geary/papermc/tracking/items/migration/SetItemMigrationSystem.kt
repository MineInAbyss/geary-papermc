package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.inventory.ItemStack

class SetItemMigrationSystem : GearyListener() {
    private val TargetScope.setItem by onSet<SetItem>()
    private val TargetScope.item by onSet<ItemStack>()

    @Handler
    fun TargetScope.handle() {
        setItem.item.toItemStack(applyTo = item)
    }
}
