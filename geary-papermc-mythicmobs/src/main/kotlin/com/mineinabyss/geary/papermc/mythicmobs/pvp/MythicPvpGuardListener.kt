package com.mineinabyss.geary.papermc.mythicmobs.pvp

import com.mineinabyss.geary.papermc.events.PlayerSkillHarmEvent
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectTypeCategory

// Potion attribution relies on the caster tracker, so any plugin-caused harmful effect applied to another player
// during a player's skill is treated as part of that skill
class MythicPvpGuardListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun MythicDamageEvent.onSkillDamage() {
        val attacker = caster.entity?.bukkitEntity as? Player ?: return
        val victim = target.bukkitEntity as? Player ?: return
        if (attacker == victim) return
        if (!PlayerSkillHarmEvent(attacker, victim, PlayerSkillHarmEvent.Kind.DAMAGE).callEvent()) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun EntityPotionEffectEvent.onSkillPotion() {
        if (cause != EntityPotionEffectEvent.Cause.PLUGIN) return
        val victim = entity as? Player ?: return
        val attacker = SkillCasterTracker.current ?: return
        if (attacker == victim) return
        if (newEffect?.type?.category != PotionEffectTypeCategory.HARMFUL) return
        if (!PlayerSkillHarmEvent(attacker, victim, PlayerSkillHarmEvent.Kind.POTION_EFFECT).callEvent()) isCancelled = true
    }
}
