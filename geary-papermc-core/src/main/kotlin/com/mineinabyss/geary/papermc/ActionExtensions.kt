package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

var ActionGroupContext.location: Location?
    get() = (environment["location"] as? Location) ?: entity?.get<BukkitEntity>()?.location
    set(value) { environment["location"] = value }

var ActionGroupContext.item: ItemStack?
    get() = (environment["item"] as? ItemStack) ?: entity?.get<ItemStack>()
    set(value) { environment["item"] = value }
