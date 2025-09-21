---
template: configurable.html
title: "player"
type: Entity
desc: Checks many conditions about a player, ex. sneaking, sprinting, flying...
params:
  sneaking:
    type: Boolean
    default: Ignore
    desc: ""
  sprinting:
    type: Boolean
    default: Ignore
    desc: ""
  blocking:
    type: Boolean
    default: Ignore
    desc: ""
  sleeping:
    type: Boolean
    default: Ignore
    desc: ""
  deeplySleeping:
    type: Boolean
    default: Ignore
    desc: ""
  swimming:
    type: Boolean
    default: Ignore
    desc: ""
  climbing:
    type: Boolean
    default: Ignore
    desc: ""
  jumping:
    type: Boolean
    default: Ignore
    desc: ""
  inLava:
    type: Boolean
    default: Ignore
    desc: ""
  inWater:
    type: Boolean
    default: Ignore
    desc: ""
  inBubbleColumn:
    type: Boolean
    default: Ignore
    desc: ""
  inRain:
    type: Boolean
    default: Ignore
    desc: ""
  flying:
    type: Boolean
    default: Ignore
    desc: ""
  gliding:
    type: Boolean
    default: Ignore
    desc: ""
  frozen:
    type: Boolean
    default: Ignore
    desc: ""
  freezeTickingLocked:
    type: Boolean
    default: Ignore
    desc: ""
  inPowderedSnow:
    type: Boolean
    default: Ignore
    desc: ""
  inCobweb:
    type: Boolean
    default: Ignore
    desc: ""
  insideVehicle:
    type: Boolean
    default: Ignore
    desc: ""
  conversing:
    type: Boolean
    default: Ignore
    desc: ""
  riptiding:
    type: Boolean
    default: Ignore
    desc: ""
  invisible:
    type: Boolean
    default: Ignore
    desc: ""
  glowing:
    type: Boolean
    default: Ignore
    desc: ""
  invurnerable:
    type: Boolean
    default: Ignore
    desc: ""
  silent:
    type: Boolean
    default: Ignore
    desc: ""
  op:
    type: Boolean
    default: Ignore
    desc: ""
---

# Examples

```yaml
check.entity:
  sneaking: true
  invisible: false
```
