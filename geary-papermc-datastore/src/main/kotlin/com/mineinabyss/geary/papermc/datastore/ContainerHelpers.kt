package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.component
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.components.Persists
import com.mineinabyss.geary.serialization.getAllPersisting
import com.mineinabyss.geary.serialization.setAllPersisting
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) = with(world) {
    val persisting = getAllPersisting()
    if (persisting.isEmpty() && getRelations<InstanceOf?, Any?>().isEmpty()) {
        pdc.hasComponentsEncoded = false
        return
    }
    // Update hashes
    persisting.forEach {
        getRelation<Persists>(component(it::class))?.hash = it.hashCode()
    }
    pdc.encodeComponents(persisting, type)
}

context(Geary)
fun GearyEntity.encodeComponentsTo(holder: PersistentDataHolder) {
    val bukkitHolder = holder as? BukkitEntity
    world.logger.v { "Encoding components for bukkit entity $id (${bukkitHolder?.type} ${bukkitHolder?.uniqueId})" }
    encodeComponentsTo(holder.persistentDataContainer)
}

context(Geary)
fun GearyEntity.encodeComponentsTo(item: ItemStack) {
    item.editPersistentDataContainer(::encodeComponentsTo)
}


/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
fun GearyEntity.loadComponentsFrom(pdc: PersistentDataContainerView) {
    loadComponentsFrom(with(world) { pdc.decodeComponents() })
}

fun GearyEntity.loadComponentsFrom(decodedEntityData: DecodedEntityData) {
    val (components, type) = decodedEntityData

    // Components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
    //TODO this should just add the id and a listener handle what addPrefab currently does
    with(world) {
        type.forEach { extend(it.toGeary()) }
    }
}

context(Geary)
fun PersistentDataHolder.decodeComponents(): DecodedEntityData =
    persistentDataContainer.decodeComponents()

context(Geary)
fun ItemStack.decodeComponents(): DecodedEntityData =
    persistentDataContainer.decodeComponents()
