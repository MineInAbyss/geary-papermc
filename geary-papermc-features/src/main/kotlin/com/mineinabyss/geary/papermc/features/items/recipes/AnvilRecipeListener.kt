package com.mineinabyss.geary.papermc.features.items.recipes

import com.destroystokyo.paper.event.block.AnvilDamagedEvent
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import kotlin.random.Random

internal class AnvilRecipeListener(private val recipes: RecipeManager) : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun PrepareAnvilEvent.onPrepareAnvil() {
        val player = view.player as? Player ?: return
        ShownResults.record(view, null)
        val craft = findCraft(view, player) ?: return

        val tooExpensive = craft.levelCost >= view.maximumRepairCost && player.gameMode != GameMode.CREATIVE
        result = craft.result.takeUnless { tooExpensive }
        ShownResults.record(view, result)
        view.repairCost = craft.levelCost
        view.repairItemCountCost = craft.recipe.material.amount
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClickAnvilResult() {
        if (slotType != InventoryType.SlotType.RESULT) return
        val view = view as? AnvilView ?: return
        val player = whoClicked as? Player ?: return
        val inventory = view.topInventory
        val craft = findCraft(view, player) ?: run {
            if (ShownResults.isShown(view, inventory.result)) isCancelled = true
            return
        }

        // Every click is cancelled, as vanilla takes the result on some clicks that are no take action, such as onto a bundle on the cursor
        isCancelled = true
        if (!isResultTake || inventory.result.isNullOrEmpty() || !hasRoomFor(player, craft.result)) return
        if (!chargeLevels(player, craft.levelCost)) return

        inventory.secondItem = craft.recipe.material.consumeFor(player, craft.material)
        inventory.firstItem = craft.recipe.input.consumeFor(player, craft.input)
        deliver(player, craft.result)
        wearAnvil(view, player)
        player.updateInventory()
    }

    private fun findCraft(view: AnvilView, player: Player): AnvilCustomRecipe.Craft? {
        val input = view.topInventory.firstItem ?: return null
        val material = view.topInventory.secondItem ?: return null
        return recipes.anvilRecipes.asSequence()
            .filter { it.hasPermission(player) && it.input.canUse(input) && it.material.canUse(material) }
            .firstNotNullOfOrNull { it.craft(input, material, view.renameText) }
    }

    private fun RecipeIngredient.consumeFor(player: Player, item: ItemStack): ItemStack? {
        val consumed = consume(item, player)
        consumed.leftover?.let(player::giveOrDrop)
        return consumed.slot
    }

    private fun chargeLevels(player: Player, cost: Int): Boolean {
        if (player.gameMode == GameMode.CREATIVE) return true
        if (player.level < cost) return false

        player.giveExpLevels(-cost)
        return true
    }

    // Mirrors AnvilMenu.onTake, which leaves blocks other than anvils alone, such as where a plugin opened a virtual anvil
    private fun wearAnvil(view: AnvilView, player: Player) {
        val block = view.topInventory.location?.block ?: return
        val damaged = player.gameMode != GameMode.CREATIVE && Tag.ANVIL.isTagged(block.type) && Random.nextFloat() < DAMAGE_CHANCE
        if (!damaged) return block.world.playEffect(block.location, Effect.ANVIL_USE, 0)

        val next = AnvilDamagedEvent.DamageState.entries[AnvilDamagedEvent.DamageState.getState(block.type).ordinal + 1]
        val event = AnvilDamagedEvent(view, next.material.takeUnless { it.isAir }?.createBlockData())
        if (!event.callEvent()) return

        if (event.isBreaking) return block.apply { type = Material.AIR }.world.playEffect(block.location, Effect.ANVIL_BREAK, 0)

        val facing = (block.blockData as Directional).facing
        block.blockData = (event.damageState.material.createBlockData() as Directional).apply { this.facing = facing }
        block.world.playEffect(block.location, Effect.ANVIL_USE, 0)
    }

    private companion object {
        // AnvilMenu's chance to damage the anvil per use
        const val DAMAGE_CHANCE = 0.12f
    }
}
