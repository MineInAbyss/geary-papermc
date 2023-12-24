package com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype

import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.idofront.nms.aliases.toNMS
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConvertEntityTypesListener : Listener {
    @EventHandler
    fun GearyEntityAddToWorldEvent.onAdd() {
        val type = gearyEntity.get<SetEntityType>() ?: return

        if (entity.toNMS().type != type.entityTypeFromRegistry) {
            UpdateMob.scheduleRecreation(entity, gearyEntity)
        }
    }
}
