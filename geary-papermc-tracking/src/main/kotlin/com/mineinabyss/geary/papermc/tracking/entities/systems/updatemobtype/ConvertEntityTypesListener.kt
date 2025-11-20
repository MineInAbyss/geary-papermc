package com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype

import com.mineinabyss.geary.papermc.MobTypeConversion
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.idofront.nms.aliases.toNMS
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConvertEntityTypesListener() : Listener {
    @EventHandler
    fun GearyEntityAddToWorldEvent.onAdd() {
        if (gearyPaper.config.mobTypeConversion == MobTypeConversion.IGNORE) return
        val type = gearyEntity.get<SetEntityType>() ?: return
        if(entity.toNMS().type == type.entityTypeFromRegistry) return

        when (gearyPaper.config.mobTypeConversion) {
            MobTypeConversion.MIGRATE -> {
                UpdateMob.scheduleRecreation(entity, gearyEntity)
            }

            MobTypeConversion.REMOVE -> {
                UpdateMob.scheduleRemove(entity)
            }

            MobTypeConversion.IGNORE ->  Unit
        }
    }
}
