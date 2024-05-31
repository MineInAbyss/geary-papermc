package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.component
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.components.Persists
import com.mineinabyss.geary.serialization.getAllPersisting
import com.mineinabyss.geary.serialization.setAllPersisting
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
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

fun GearyEntity.encodeComponentsTo(holder: PersistentDataHolder) {
    val bukkitHolder = holder as? BukkitEntity
    geary.logger.v("Encoding components for bukkit entity $id (${bukkitHolder?.type} ${bukkitHolder?.uniqueId})")
    encodeComponentsTo(holder.persistentDataContainer)
}

fun GearyEntity.encodeComponentsTo(item: ItemStack) {
    item.editMeta { encodeComponentsTo(it.persistentDataContainer) }
}


/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
fun GearyEntity.loadComponentsFrom(pdc: PersistentDataContainer) {
    loadComponentsFrom(pdc.decodeComponents())
}

fun GearyEntity.loadComponentsFrom(decodedEntityData: DecodedEntityData) {
    val (components, type) = decodedEntityData

    // Components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
    //TODO this should just add the id and a listener handle what addPrefab currently does
    type.forEach { extend(it.toGeary()) }
}

fun PersistentDataHolder.decodeComponents(): DecodedEntityData =
    persistentDataContainer.decodeComponents()

fun ItemStack.decodeComponents(): DecodedEntityData =
    itemMeta.decodeComponents()
