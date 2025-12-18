# Arena - Minecraft Arena Plugin

A Paper 1.21 survival arena plugin where players enter procedurally generated arena worlds through portals to fight waves of increasingly powerful mobs.

## Key Features

- Procedural World Generation: Simplex noise terrain variation + preset chunk templates
- Event-Driven Architecture: No polling, state transitions via Bukkit event listeners
- Async Task Scheduling: Mob spawn detection and player monitoring run asynchronously, zero main thread blocking
- Dynamic Difficulty: Mob health and equipment quality scale with challenge count

## Project Structure

```
arena/
├── Arena.java              # Plugin entry, world initialization
├── WorldManager.java       # World creation/reset, custom ChunkGenerator
├── ChunkTemplate.java      # Chunk template data, Simplex noise terrain
├── BlockPropertyUtil.java  # Block property encoding/decoding (direction, slab type)
├── MobChain.java           # Wave control, mob spawning, victory detection
├── MobContainer.java       # Mob type definitions, Team management
├── PortalListener.java     # Portal events, death/transform event handling
└── SaveChunkTemplateCommand.java  # Dev tool: export chunk as template code
```

## Performance Design

| Module | Design | Performance Benefit |
|--------|--------|---------------------|
| World Gen | `ChunkGenerator.generateNoise()` override | Executes only on first chunk load, zero runtime overhead |
| Wave Monitor | `BukkitRunnable.runTaskTimerAsynchronously()` | Async thread execution, no main thread tick blocking |
| Boss Detection | `CompletableFuture` + main thread callback | Avoids async direct Bukkit API access |
| Mob Management | Scoreboard Team | Native API for entity grouping, no extra data structures |
| Template Storage | 1D arrays `int[] + Material[]` | Memory compact, efficient iteration |

## Gameplay Flow

1. Player stands on gilded blackstone and enters nether portal → teleports to arena
2. 45-second preparation (crouch on yellow terracotta to exit)
3. 5 waves of mobs, 12-tick spawn cycle per wave
4. Wave 5 is boss fight (Warden / Iron Golem)
5. Victory triggers fireworks, next round begins (increased difficulty)

## Commands

- `/arena` - Export current chunk as template code (dev use)

## Dependencies

- Paper API 1.21.10
- Java 21
