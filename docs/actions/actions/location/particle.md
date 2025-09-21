---
template: configurable.html
title: particle
type: Location
desc: Spawns a particle
params:
  particle:
    type: Particle
  offsetX:
    type: Double
    default: "0"
  offsetY:
    type: Double
    default: "0"
  offsetZ:
    type: Double
    default: "0"
  color:
    type: Color
    desc: Optional color parameter for particles that support it
    default: "null"
  count:
    type: Int
    default: "1"
  radius:
    type: Int
    desc: The radius in which players should be able to see the particle
    default: "32"
  speed:
    type: Double
    desc: The movement speed of the particle
    default: "0.0"
  at:
    type: Location
    desc: Where to spawn the particle
---

## Examples

```yaml
particle:
  at: "{{ entity.getLocation }}"
  particle: CLOUD
  offsetY: 1
  count: 3
```
