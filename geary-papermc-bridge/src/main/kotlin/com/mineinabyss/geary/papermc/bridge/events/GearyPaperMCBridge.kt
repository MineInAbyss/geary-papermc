package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.addons.Application
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.papermc.bridge.events.entities.EntityDamageBridge
import com.mineinabyss.geary.papermc.bridge.events.entities.EntityLoadUnloadBridge
import com.mineinabyss.geary.papermc.bridge.events.entities.EntityShearedBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemRemovedBridge
import com.mineinabyss.idofront.plugin.listeners

fun Application.paperMCBridge() {
    onPluginEnable {
        listeners(
            EntityDamageBridge(),
            EntityLoadUnloadBridge(),
            EntityShearedBridge(),
            ItemInteractBridge(),
            ItemRemovedBridge(),
        )
    }
}
