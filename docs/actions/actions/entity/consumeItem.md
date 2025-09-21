---
template: configurable.html
title: consumeItem
type: Entity
desc: Consumes an item from the entity's inventory
params:
  type:
    type: ItemStack
    desc: The item to consume
  amount:
    type: Int
    desc: The amount of the item to consume
    default: 1
---

## Examples

```yaml
consumeItem:
  type: mineinabyss:flamethrower_fuel
```
