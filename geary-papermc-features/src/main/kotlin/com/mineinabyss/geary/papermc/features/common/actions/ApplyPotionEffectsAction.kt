package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect

@Serializable(with = ApplyPotionEffectsAction.Serializer::class)
class ApplyPotionEffectsAction(
    val effects: List<@Serializable(with = PotionEffectSerializer::class) PotionEffect>,
) : Action {
    override fun ActionGroupContext.execute() {
        val bukkit = entity?.get<BukkitEntity>() as? LivingEntity ?: return
        bukkit.addPotionEffects(effects)
    }

    object Serializer: InnerSerializer<List<PotionEffect>, ApplyPotionEffectsAction>(
        serialName = "geary:apply_potion_effects",
        inner = ListSerializer(PotionEffectSerializer),
        transform = { ApplyPotionEffectsAction(it) },
        inverseTransform = { it.effects }
    )
}
