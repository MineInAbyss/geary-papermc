package com.mineinabyss.geary.papermc.features.general.cooldown

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary

object CooldownFeature : GearyAddonWithDefault<CooldownFeature> {
    override fun CooldownFeature.install() {
        geary.run {
            createCooldownDisplaySystem()
        }
    }

    override fun default() = CooldownFeature
}
