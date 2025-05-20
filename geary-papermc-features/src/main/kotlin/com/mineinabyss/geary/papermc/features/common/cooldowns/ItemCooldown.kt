package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.GameMode
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:item_cooldown")
class ItemCooldown(
    val ignoreCreative: Boolean = true,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = entity?.parent?.get<Player>()?.takeUnless { it.gameMode == GameMode.CREATIVE && ignoreCreative } ?: return true
        val item = entity?.get<SetItem>()?.item?.toItemStackOrNull() ?: return true
        val cooldown = item.getData(DataComponentTypes.USE_COOLDOWN) ?: return true
        if (player.hasCooldown(item)) return false

        player.setCooldown(item, cooldown.seconds().times(20).toInt())
        return true
    }
}