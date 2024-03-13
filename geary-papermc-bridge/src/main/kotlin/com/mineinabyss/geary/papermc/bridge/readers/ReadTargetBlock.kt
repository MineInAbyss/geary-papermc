package com.mineinabyss.geary.papermc.bridge.readers

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity


@Serializable
@SerialName("geary:read.target_block")
class ReadTargetBlock(
    val maxDistance: Int,
)

fun GearyModule.createTargetBlockReader() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        val read by source.get<ReadTargetBlock>()
    }
).exec {
    val targetBlock = (bukkit as? LivingEntity)?.getTargetBlock(null, read.maxDistance) ?: return@exec
    event.entity.set(targetBlock.location)

}
