package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun Geary.markAsNexoFurniture() = observe<OnSet>()
    .involving(query<SetNexoFurniture, ItemDisplay>())
    .exec { (nexoFurniture, bukkitEntity) ->
        bukkitEntity.persistentDataContainer.set(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING, nexoFurniture.id.full)
    }

fun Geary.markAsNexoItem() = observe<OnSet>()
    .involving(query<SetNexoFurniture, ItemStack>())
    .exec { (nexoFurniture, itemStack) ->
        itemStack.editPersistentDataContainer { it.set(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING, nexoFurniture.id.full) }
    }