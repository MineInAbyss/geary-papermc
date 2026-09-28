package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.component.DyedItemColor
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal fun ItemStack?.isNullOrEmpty(): Boolean {
    contract { returns(false) implies (this@isNullOrEmpty != null) }
    return this == null || isEmpty
}

/** Null for an item that cannot be damaged */
internal fun ItemStack.remainingDurability(): Int? {
    if (hasData(DataComponentTypes.UNBREAKABLE)) return null
    val maxDamage = getData(DataComponentTypes.MAX_DAMAGE) ?: return null
    return maxDamage - (getData(DataComponentTypes.DAMAGE) ?: 0)
}

internal fun ItemStack.craftingRemainder(): ItemStack? = type.asItemType()?.craftingRemainingItem?.createItemStack()

internal fun ItemStack.hoverNameText(): String = PlainTextComponentSerializer.plainText().serialize(effectiveName())

internal fun ItemStack.dyedWith(dyes: List<ItemStack>): ItemStack {
    val colors = dyes.map { CraftItemStack.unwrap(it).getOrDefault(DataComponents.DYE, DyeColor.WHITE) }
    return CraftItemStack.asBukkitCopy(DyedItemColor.applyDyes(CraftItemStack.unwrap(this), colors))
}

internal fun ItemStack.inStacks(total: Int): List<ItemStack> =
    (total downTo 1 step maxStackSize).map { asQuantity(minOf(it, maxStackSize)) }

internal fun Player.giveOrDrop(item: ItemStack) {
    inventory.addItem(item).forEach { (_, overflow) -> world.dropItemNaturally(location, overflow) }
}
