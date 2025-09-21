# Technical overview

Geary-papermc provides a spawning system, with integrations for MythicMobs mobs. This page goes over exactly how the spawning system picks blocks and entities to spawn.
The system runs each tick, on all players on the server with the following logic:

### Location picking

- Find a position minDistance to maxDistance horizontally away from the player, as well as maxVerticalDistance above or below the player.
    - Occasionally bias towards a GROUND location by getting the highest solid block in the allowed vertical range.
- Ensure this location is at least minDistance away from ALL other players.
- Categorize the location into one of the following spawn position types: GROUND, IN_BLOCK, AIR, WATER, LAVA.

### Spawn picking

- Check all WorldGuard regions at the chosen location and pick any spawns matching these regions.
- Filter these spawns by the block's spawn position.
- Calculate nearby mob caps and filter out any spawns whose types exceed these caps.
- Use a weighted random selection to pick a spawn from the remaining spawns, based on the spawn's `priority`.
- Ensure its additional conditions are met. Don't retry if the spawn is invalid, since we want lower priority to mean less common spawns

### Spawn spread

- For spawns with a spread or ySpread defined, use a normal distribution with standard deviation equal to the spread to pick a new location, if the location's position type is the same as the spawn's, try to spawn there.
- For any spawn that would cause a mob to overlap with a block's hitbox, leading to suffocation, attempt to move the entity up to avoid this, removing the entity if no valid spawn position is found.
