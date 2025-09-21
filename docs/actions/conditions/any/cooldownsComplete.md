---
template: configurable.html
title: cooldownsComplete
type: Entity
desc: Checks whether entity has no cooldowns specified by name
params:
  self:
    type: List String 
    desc: List of cooldown names to check
---

## Examples

```yaml
cooldownsComplete: [ "explosion" ]
```
