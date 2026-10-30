package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

internal class RecipeIngredient(
    val display: ItemStack,
    val amount: Int = 1,
    val consumption: Consumption = Consumption.Consume(),
    val preserved: List<PreservedComponentData> = emptyList(),
    val removed: List<RemovedComponentData> = emptyList(),
    val isPrefab: Boolean = false,
    private val matcher: (ItemStack) -> Boolean,
    val choice: RecipeChoice = RecipeChoice.predicateChoice({ !it.isEmpty && matcher(it) }, display),
) {
    val isCustomConsumption: Boolean get() = amount > 1 || consumption != Consumption.Consume()

    fun matches(item: ItemStack?): Boolean = !item.isNullOrEmpty() && matcher(item)

    fun canUse(item: ItemStack?, ignoreAmount: Boolean = false): Boolean {
        if (item == null || !matches(item)) return false
        return (ignoreAmount || item.amount >= amount) && hasDurabilityFor(item)
    }

    private fun hasDurabilityFor(item: ItemStack): Boolean {
        val durability = (consumption as? Consumption.Damage)?.durability ?: return true
        if (item.hasData(DataComponentTypes.UNBREAKABLE)) return true
        val remaining = item.remainingDurability() ?: return false
        return remaining >= durability
    }

    fun consume(item: ItemStack, entity: LivingEntity?, take: Int = amount): Consumed = when (consumption) {
        Consumption.Keep -> Consumed(item, null)
        is Consumption.Damage -> Consumed(damaged(item, entity, consumption.durability) ?: consumption.replacement?.clone(), null)
        is Consumption.Consume -> used(item, take, consumption.replacement ?: item.craftingRemainder())
    }

    private fun used(item: ItemStack, take: Int, replacement: ItemStack?): Consumed {
        // One per item taken, so the count does not depend on how a shapeless amount is spread over slots
        val replaced = replacement?.let { it.asQuantity(it.amount * take) }
        val remaining = item.amount - take
        if (remaining > 0) return Consumed(item.asQuantity(remaining), replaced)
        if (replaced == null || replaced.amount <= replaced.maxStackSize) return Consumed(replaced, null)
        return Consumed(replaced.asQuantity(replaced.maxStackSize), replaced.asQuantity(replaced.amount - replaced.maxStackSize))
    }

    private fun damaged(item: ItemStack, entity: LivingEntity?, durability: Int): ItemStack? {
        if (entity != null) return item.clone().damage(durability, entity).takeUnless(ItemStack::isEmpty)
        if (item.hasData(DataComponentTypes.UNBREAKABLE)) return item

        val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE) ?: return item
        val newDamage = (item.getData(DataComponentTypes.DAMAGE) ?: 0) + durability
        if (newDamage >= maxDamage) return null
        return item.clone().apply { setData(DataComponentTypes.DAMAGE, newDamage) }
    }

    fun withOptions(
        amount: Int = this.amount,
        consumption: Consumption = this.consumption,
        preserved: List<PreservedComponentData> = this.preserved,
        removed: List<RemovedComponentData> = this.removed,
    ) = RecipeIngredient(display, amount, consumption, preserved, removed, isPrefab, matcher, choice)

    data class Consumed(val slot: ItemStack?, val leftover: ItemStack?)
}

internal sealed interface Consumption {
    data class Consume(val replacement: ItemStack? = null) : Consumption

    data class Damage(val durability: Int, val replacement: ItemStack? = null) : Consumption

    data object Keep : Consumption
}
