package com.mineinabyss.geary.papermc.features.items.recipes

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.datacomponent.DataComponentTypes
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.Recipe

/** A mocked player whose inventory, drops and discovered recipes are real state tests can read back */
internal class TestPlayer(
    val permissions: Set<String> = emptySet(),
    val lastLogin: Long = 0,
) {
    val storage: Inventory = CraftInventoryCustom(null, 36)
    var offHand: ItemStack = ItemStack.empty()
    val dropped = mutableListOf<ItemStack>()
    val discovered = mutableListOf<NamespacedKey>()
    val crafted = mutableListOf<NamespacedKey>()

    val inventory: PlayerInventory = mockk(relaxed = true) {
        every { storageContents } answers { storage.storageContents }
        every { contents } answers { storage.contents }
        every { addItem(*anyVararg()) } answers { storage.addItem(*args.filterIsInstance<Array<ItemStack>>().single()) }
        every { getItem(any<Int>()) } answers { storage.getItem(firstArg()) }
        every { setItem(any<Int>(), any()) } answers { storage.setItem(firstArg(), secondArg()) }
        every { itemInOffHand } answers { offHand }
        every { setItemInOffHand(any()) } answers { offHand = firstArg<ItemStack?>() ?: ItemStack.empty() }
    }

    // A field rather than a getter, so a relaxed mock would leave it null
    val handle: ServerPlayer = mockk<ServerPlayer>(relaxed = true).also { it.connection = mockk(relaxed = true) }

    val player: CraftPlayer = mockk(relaxed = true) {
        every { handle } returns this@TestPlayer.handle
        every { inventory } returns this@TestPlayer.inventory
        every { hasPermission(any<String>()) } answers { firstArg<String>() in permissions }
        every { location } returns Location(null, 0.0, 0.0, 0.0)
        every { world.dropItemNaturally(any(), any<ItemStack>()) } answers {
            dropped += secondArg<ItemStack>()
            mockk(relaxed = true)
        }
        every { discoverRecipes(any()) } answers { firstArg<Collection<NamespacedKey>>().also { discovered += it }.size }
        every { discoverRecipe(any()) } answers {
            crafted += firstArg<NamespacedKey>()
            true
        }
        // Paper's ItemStack.damage hands off to the entity, this mirrors it for items without enchantments
        every { damageItemStack(any<ItemStack>(), any()) } answers {
            val item = firstArg<ItemStack>()
            val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE)
            val damage = (item.getData(DataComponentTypes.DAMAGE) ?: 0) + secondArg<Int>()
            when {
                maxDamage == null || item.hasData(DataComponentTypes.UNBREAKABLE) -> item
                damage >= maxDamage -> ItemStack.empty()
                else -> item.apply { setData(DataComponentTypes.DAMAGE, damage) }
            }
        }
        every { isOnline } returns true
        every { lastLogin } returns this@TestPlayer.lastLogin
    }

    fun count(material: Material): Int = storage.contents.filterNotNull().filter { it.type == material }.sumOf { it.amount } +
        offHand.takeIf { it.type == material }?.amount.orZero()

    private fun Int?.orZero() = this ?: 0
}

/** Backed by a real inventory, [recipe] is what vanilla picks for the current matrix unless [pin] fixes it */
internal class TestCraftingInventory(size: Int = 9, private val backing: Inventory = CraftInventoryCustom(null, size)) :
    CraftingInventory, Inventory by backing {
    private var result: ItemStack? = null
    private var pinned: Recipe? = null
    private var isPinned = false

    override fun getResult(): ItemStack? = result
    override fun setResult(newResult: ItemStack?) {
        result = newResult
    }

    override fun getMatrix(): Array<ItemStack?> = backing.contents
    override fun setMatrix(contents: Array<out ItemStack?>) = backing.setContents(contents)

    override fun getRecipe(): Recipe? = if (isPinned) pinned else PaperTestServer.vanillaCraftingPick(matrix)

    fun pin(recipe: Recipe?) {
        isPinned = true
        pinned = recipe
    }
}

internal class Cursor(var item: ItemStack = ItemStack.empty())

internal fun craftingView(player: TestPlayer, inventory: CraftingInventory, cursor: Cursor = Cursor()): InventoryView = mockk(relaxed = true) {
    every { topInventory } returns inventory
    every { this@mockk.player } returns player.player
    every { this@mockk.cursor } answers { cursor.item }
    every { setCursor(any()) } answers { cursor.item = firstArg<ItemStack?>() ?: ItemStack.empty() }
}

internal fun resultClick(
    view: InventoryView,
    action: InventoryAction = InventoryAction.PICKUP_ALL,
    click: ClickType = if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) ClickType.SHIFT_LEFT else ClickType.LEFT,
    hotbarButton: Int = -1,
) = InventoryClickEvent(view, InventoryType.SlotType.RESULT, 0, click, action, hotbarButton)
