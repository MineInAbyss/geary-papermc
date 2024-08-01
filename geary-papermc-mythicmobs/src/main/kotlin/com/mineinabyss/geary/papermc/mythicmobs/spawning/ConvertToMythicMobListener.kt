package com.mineinabyss.geary.papermc.mythicmobs.spawning

import com.mineinabyss.geary.papermc.MobTypeConversion
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import io.lumine.mythic.api.mobs.MythicMob
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.constants.MobKeys
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConvertToMythicMobListener : Listener {
    @EventHandler
    fun GearyEntityAddToWorldEvent.onAdd() {
        if (gearyPaper.config.mobTypeConversion == MobTypeConversion.IGNORE) return
        val mm = MythicBukkit.inst()
        if (mm.mobManager == null) return

        // If MM already encoded or loaded, we let it handle things itself
        if (entity.persistentDataContainer.has(MobKeys.TYPE)) return
        if (mm.mobManager.isActiveMob(entity.uniqueId)) return

        if (!gearyEntity.has<SetMythicMob>()) return
        if (gearyEntity.has<MythicMob>()) return

        when (gearyPaper.config.mobTypeConversion) {
            MobTypeConversion.MIGRATE -> {
                UpdateMob.scheduleRecreation(entity, gearyEntity)
            }

            MobTypeConversion.REMOVE -> {
                UpdateMob.scheduleRemove(entity)
            }

            MobTypeConversion.IGNORE -> Unit
        }
    }
}
