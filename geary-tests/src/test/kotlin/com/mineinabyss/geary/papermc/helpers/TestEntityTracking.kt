package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingConfiguration

val TestEntityTrackingConfiguration: EntityTrackingConfiguration.() -> Unit = {
    forceMainThread = false
}
