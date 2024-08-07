package com.mineinabyss.geary.papermc.features.entities.displayname

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*

class ShowDisplayNameOnKillerListener : Listener {
    @EventHandler
    fun PlayerDeathEvent.replaceMobName() {
        val message = (deathMessage() as TranslatableComponent)
        val args = message.args().toMutableList()
        val entityIndex = args.indexOfFirst { (it is TranslatableComponent) && it.key().startsWith("entity") }
        if (entityIndex == -1) return

        val killer =
            Bukkit.getEntity(UUID.fromString((args[entityIndex] as TranslatableComponent).insertion())) ?: return
        val name = killer.toGeary().get<com.mineinabyss.geary.papermc.features.entities.displayname.DisplayName>()?.name ?: return
        args[entityIndex] = name.miniMsg()
        val newMsg = message.args(args)
        deathMessage(newMsg)
    }
}
