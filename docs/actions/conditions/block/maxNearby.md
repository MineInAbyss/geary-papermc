---
template: configurable.html
title: maxNearby
type: Block
desc: Checks whether there are too many entities of a certain type near this block
params:
  amount:
    type: Int
    desc: Maximum amount to allow
  types:
    type: List String 
    desc: List of entity types to check for
    default: "<i>The type specified by a mob spawn</i>"
  radius:
    type: Double
    desc: Radius to count entities in
    default: "128"
---

## Examples

```yaml
maxNearby:
  amount: 3
```

```yaml
maxNearby:
  amount: 2
  radius: 10
  types: [ "mm:myMythicMob" ]
```
