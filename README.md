# FKothWins

FKothWins is a faction-focused Minecraft plugin designed to track and reward **King of the Hill (KoTH) victories** at the faction level.

It gives server owners a clear, competitive leaderboard metric that can be integrated into faction progression, community events, and seasonal ranking systems.

## Value Proposition

- Adds long-term competitive value to KoTH events.
- Tracks wins as a faction asset, not as an individual stat.
- Supports transparent rankings and performance visibility.
- Improves player engagement through measurable faction goals.

## Core Functional Outcomes

- Automatic win attribution from KoTH winner to the winner’s faction.
- Administrative controls for adding, removing, or setting faction wins.
- In-game faction statistics and top ranking views.
- Top 5 hologram support for public ranking visibility (e.g. warp PvP).
- Placeholder support for faction profile pages, scoreboards, and overlays.

## Governance Rules

- Players without a faction do not generate a valid win.
- Wins remain with the faction that earned them.
- If a faction is disbanded, its stored wins are removed.
- Offline winner lookups are supported through command flow configuration.

## Command Surface

- Admin / Console:
  - `/fkoth add <player> <amount>`
  - `/fkoth remove <faction> <amount>`
  - `/fkoth set <faction> <amount>`
  - `/fkoth reload`
  - `/fkoth debug`
- Player:
  - `/fkoth stats`
  - `/fkoth top`

## Placeholder Coverage

- `%fkoth_faction_name%`
- `%fkoth_faction_wins%`
- `%fkoth_faction_rank%`
- `%fkoth_top_size%`
- `%fkoth_top_1_name%` ... `%fkoth_top_10_name%`
- `%fkoth_top_1_wins%` ... `%fkoth_top_10_wins%`
- `%fkoth_player_faction_wins_<player>%` (player-targeted faction wins)

## Integrations

- KoTH event integration (automatic winner processing)
- SaberFactions / Factions-compatible faction resolution
- PlaceholderAPI expansion
- DecentHolograms top display integration

## Delivery & Operations

This repository includes:
- Standardized Git hooks for quality checks before commit/push.
- CI pipeline for validation and build.
- Release pipeline for tagged versions and packaged artifact delivery.
