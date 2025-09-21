---
template: configurable.html
title: applyPotionEffects
type: Entity
desc: Applies potion effects
params:
  self:
    type: List<PotionEffect>
    desc: The effects to apply
---

## Examples

```yaml
applyPotionEffects:
  - type: minecraft:speed
    duration: 3s
    amplifier: 2
    hasParticles: false
  - type: minecraft:jump_boost
    duration: 5s
    amplifier: 2
    hasParticles: false
```
