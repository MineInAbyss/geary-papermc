---
template: configurable.html
title: explode
type: Location
desc: Creates an explosion
params:
  breakBlocks:
    type: Boolean
    desc: Whether to break blocks
    default: true
  setFire:
    type: Boolean
    desc: Whether to set fire to blocks
    default: true
  power:
    type: Double
    desc: The strength of the explosion
    default: 1
  at:
    type: Location
    desc: Where to spawn the explosion
---

## Examples

```yaml
explode:
  breakBlocks: false
  power: 2
  at: "{{ entity.getTargetBlock { maxDistance: 10 } }}"
```
