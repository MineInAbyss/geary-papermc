---
template: configurable.html
title: ensure
type: Any
desc: Checks if one or more conditions are met, if not stops the action group
params:
  condition name:
    type: Condition
    desc: One or more conditions to check
---

## Examples

```yaml
ensure:
  hasConsumable:
    type: mineinabyss:flamethrower_fuel
```
