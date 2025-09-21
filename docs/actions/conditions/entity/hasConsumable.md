---
template: configurable.html
title: hasConsumable
type: Entity
desc: Checks whether the entity's inventory has a specified item
params:
  type:
    type: ItemStack
    desc: The item to check for
  amount:
    type: Int
    desc: The amount of the item to check for
    default: 1
---

# Examples

```yaml
hasConsumable:
  type: mineinabyss:flamethrower_fuel
  amount: 2
```
