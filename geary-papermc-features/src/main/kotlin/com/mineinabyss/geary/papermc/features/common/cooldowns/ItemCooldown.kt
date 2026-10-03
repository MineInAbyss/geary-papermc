package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.KeySerializer
import com.mineinabyss.idofront.time.inWholeTicks
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.bukkit.GameMode
import org.bukkit.entity.Player
import kotlin.time.Duration

/**
 * Without [group], the cooldown is applied to the item itself, which is what the client renders.
 * A [group] the item does not carry is tracked by the server but has no overlay,
 * so it can gate a secondary action on an item that already uses its own cooldown.
 * A [group] also works once the actions have become the player, so it can sit after conditions that should not consume it
 */
@Serializable
@SerialName("geary:item_cooldown")
class ItemCooldown(
    val ignoreCreative: Boolean = true,
    val group: @Serializable(with = KeySerializer::class) Key? = null,
    val duration: @Serializable(with = DurationSerializer::class) Duration? = null,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = (entity?.get<Player>() ?: entity?.parent?.get<Player>())
            ?.takeUnless { it.gameMode == GameMode.CREATIVE && ignoreCreative } ?: return true
        val item = entity?.get<SetItem>()?.item?.toItemStackOrNull()
        val useCooldown = item?.getData(DataComponentTypes.USE_COOLDOWN)
        val ticks = duration?.inWholeTicks?.toInt() ?: useCooldown?.let { (it.seconds() * 20).toInt() } ?: return true

        when {
            group != null -> {
                if (player.getCooldown(group) > 0) return false
                player.setCooldown(group, ticks)
            }
            item != null && useCooldown != null -> {
                if (player.hasCooldown(item)) return false
                player.setCooldown(item, ticks)
            }
            else -> return true
        }
        return true
    }
}
