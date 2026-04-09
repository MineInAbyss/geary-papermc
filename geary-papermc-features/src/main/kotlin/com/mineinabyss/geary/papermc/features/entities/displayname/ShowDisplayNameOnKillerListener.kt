package com.mineinabyss.geary.papermc.features.entities.displayname

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*

class ShowDisplayNameOnKillerListener : Listener {
    @EventHandler
    fun PlayerDeathEvent.replaceMobName() {
        val message = (deathMessage() as TranslatableComponent)
        val args = message.arguments().toMutableList()
        val entityIndex = args.indexOfFirst { it is TranslatableComponent && it.key().startsWith("entity") }
        if (entityIndex == -1) return

        val uuid = (args[entityIndex] as TranslatableComponent).insertion()
        val killer = Bukkit.getEntity(UUID.fromString(uuid)) ?: return
        val name = killer.toGeary().get<DisplayName>()?.name ?: return
        args[entityIndex] = TranslationArgument.component(name.miniMsg())
        val newMsg = message.arguments(args)
        deathMessage(newMsg)
    }
}
