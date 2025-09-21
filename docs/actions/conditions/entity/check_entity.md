---
template: configurable.html
title: "check.entity"
type: Entity
desc: Checks many conditions about a Minecraft entity, ex onGround, jumping, inWater...
params:
  sleeping:
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
  onGround:
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
  leashed:
    type: Boolean
    default: Ignore
    desc: ""
---

# Examples

```yaml
check.entity:
  leashed: true
  inWater: false
```
