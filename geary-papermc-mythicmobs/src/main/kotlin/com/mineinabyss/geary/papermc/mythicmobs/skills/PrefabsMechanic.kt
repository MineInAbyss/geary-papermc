package com.mineinabyss.geary.papermc.mythicmobs.skills

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.BukkitAdapter


class PrefabsMechanic(
    val prefabs: List<String>,
): ITargetedEntitySkill {
    constructor(config: MythicLineConfig): this(
        prefabs = config.getStringList(arrayOf("prefabs", "p"), "").toList()
    )

    override fun castAtEntity(meta: SkillMetadata?, target: AbstractEntity?): SkillResult {
        val bukkit = BukkitAdapter.adapt(target)
        gearyPaper.plugin.launch {
            prefabs.mapNotNull { PrefabKey.of(it).toEntityOrNull() }
                .forEach(bukkit.toGeary()::extend)
        }
        return SkillResult.SUCCESS
    }
}
