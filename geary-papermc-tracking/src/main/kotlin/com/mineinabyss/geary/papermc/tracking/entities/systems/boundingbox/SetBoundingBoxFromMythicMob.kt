package com.mineinabyss.geary.papermc.tracking.entities.systems.boundingbox

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.nms.aliases.NMSEntityType
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.NamespacedKey
import org.bukkit.util.BoundingBox
import kotlin.jvm.optionals.getOrNull

fun GearyModule.setBoundingBoxFromMythicMob() = listener(
    object : ListenerQuery() {
        val mobType by get<SetMythicMob>()
        override fun ensure() = event.anySet(::mobType)
    }
).exec {
    val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobType.id).getOrNull() ?: return@exec

//    val megModel = mythicMob.model.config.getString("Id")
    val vanillaType = NamespacedKey.minecraft(mythicMob.entityType.name.lowercase())

    val nmsType = NMSEntityType.byString(vanillaType.toString())
        .orElseGet { error("An entity type with key $vanillaType was not found.") }

    entity.addRelation<NoInherit, BoundingBox>()
    entity.set(BoundingBoxHelpers.getForEntityType(nmsType))
}
