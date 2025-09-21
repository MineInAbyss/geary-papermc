# Items

Geary provides item tracking in player inventories. For any other case, you may use the item's [PDC](../persistentdatacontainer) to read/write data, but no entity will be tracked in Geary.

## Tracking

The player inventory will automatically load items with serialized prefabs. If an item is removed, the entity gets removed with it.

## Getting an item entity

### GearyInventory

GearyInventory provides functions similar to `player.inventory`, but they return GearyEntity.

```kotlin
val player: Player

val inv = player.inventory.toGeary()

inv.itemInMainHand
inv.get(EquipmentSlot.HEAD)
inv.itemOnCursor // only works in survival mode
inv.get(10)
```

### Modifying an ItemStack
```kotlin
val gearyItem: GearyEntity

// changes are reflected in inventory
gearyItem.get<ItemStack>().amount = 10
```
