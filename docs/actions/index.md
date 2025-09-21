# Actions & Observers

The action system is used across items, entities, and blocks to code custom effects. Actions can run on specific events using the `observe` component (ex. a player clicking a custom item), or repeat passively using the `passive` component.

## Observe events

Any Geary prefab can observe events using the `observe` component. It takes a map of event names found in [[Events]] to an Action Group.

### Examples

```yaml
observe:
  itemLeftClick:
    - become: parent
    - sendActionBar: "Right clicked item!"
```

## Passive actions

Passive actions run continuously on a Geary prefab, optionally matching against certain components. The `passive` component takes a list of entries as described below

### Parameters

| Name  | Type         | Description                                                                                  |
|-------|--------------|----------------------------------------------------------------------------------------------|
| match | List String | A list of component names an instance of this prefab must have for the passive system to run |
| every | Duration     | How often to run this action group                                                           |
| run   | ActionGroup  | The actions to run when an instance is matched                                               |

### Examples

```yaml
passive:
  - match: [ inInventory ]
    every: 1s
    run:
      - become: parent
      - ensure:
          mythicConditions:
            - health{h=<5}
      - particle:
          at: "{{ entity.getLocation }}"
          particle: CLOUD
          offsetY: 1
          count: 3
          speed: 0
      - applyPotionEffects:
          - type: minecraft:speed
            duration: 3s
            amplifier: 2
            hasParticles: false
```
