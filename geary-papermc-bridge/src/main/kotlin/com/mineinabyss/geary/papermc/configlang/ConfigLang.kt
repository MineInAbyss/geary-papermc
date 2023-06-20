package com.mineinabyss.geary.papermc.configlang

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.configlang.systems.*

@Deprecated("Rework coming soon!")
class ConfigLang {
    companion object : GearyAddonWithDefault<ConfigLang> {
        override fun default() = ConfigLang()

        override fun ConfigLang.install() {
            geary.pipeline.addSystems(
                ConditionsToRoles(),
                EventRunBuilderToRelation(),
                EventRunListener(),
                ParseApply(),
                TriggersToRoles(),
                TriggerWhenSourceListener(),
                TriggerWhenTargetListener()
            )
        }
    }
}
