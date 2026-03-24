# Development Journey

This document tracks the development progress, decisions, and changes made during implementation.

## 2026-03-23

### Planning Phase
- Created project design documentation (`application-design.md`)
- Defined development plan with 6 phases (`dev-plan.md`)
- Documented future features backlog (`future-features.md`)
- Copied tile assets to project resources
- Documented tile asset structure (`tile-assets.md`)
- Set up `.windsurfrules` with project context and coding standards
- Initialized git repository on dev branch
- Committed initial planning work

### Phase 1: Core Game Engine - Started

#### Task: Set up Spring Boot project structure
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `pom.xml` with Spring Boot 3.2.0 and dependencies:
  - Spring Boot Web (REST API)
  - Spring Boot Data JPA (database access)
  - H2 Database (file-based persistence)
  - Spring Boot Validation
  - Lombok (reduce boilerplate)
  - SpringDoc OpenAPI (API documentation)
  - Spring Boot Test (JUnit 5)
- Created `application.properties` with configuration:
  - Server port 8080
  - H2 database file storage at `./data/mahjong`
  - H2 console enabled at `/h2-console`
  - Logging configuration
  - API documentation at `/swagger-ui.html`
- Created main application class `MahjongServerApplication.java`
- Base package: `com.mahjong`

#### Task: Implement tile models
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `TileSuit` enum (MANZU, PINZU, SOUZU, HONOR)
- Created `TileType` enum with all 34 tile types:
  - 9 Manzu (characters) tiles (M1-M9)
  - 9 Pinzu (circles) tiles (P1-P9)
  - 9 Souzu (bamboo) tiles (S1-S9)
  - 7 Honor tiles (4 winds + 3 dragons)
  - Helper methods: isTerminal(), isHonor(), isWind(), isDragon(), isSimple()
- Created `Tile` class with:
  - TileType and red dora flag
  - Convenience methods delegating to TileType
  - isTerminalOrHonor() helper
  - toString() for debugging
- Created comprehensive unit tests in `TileTest.java`:
  - Test tile creation (normal and red tiles)
  - Test suit and value getters
  - Test terminal, honor, wind, dragon, simple tile detection
  - Test terminalOrHonor detection
  - Test toString() output

#### Task: Create game state data structures
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `Wind` enum with:
  - Four wind directions (EAST, SOUTH, WEST, NORTH)
  - next() and previous() methods for rotation
  - toTileType() conversion method
- Created `MeldType` enum (CHI, PON, KAN_OPEN, KAN_CLOSED, KAN_ADDED)
- Created `Meld` class with:
  - Type, tiles list, and calledFrom wind
  - isOpen(), isClosed(), isKan() helper methods
- Created `Player` class with:
  - Wind position, hand, discards, melds, riichi status, score
  - drawnTile field for current draw
  - getHandSize() and getAllHandTiles() methods
  - hasOpenMelds() and isMenzen() (closed hand) detection
  - Default score of 25000 points
- Created `GameState` class with:
  - Game ID, round wind, round number, honba/riichi sticks
  - Players map (by Wind), current player tracking
  - Wall, dead wall, dora indicators, ura-dora indicators
  - Wall index for tracking draw position
  - Helper methods: getCurrentPlayer(), getPlayer(), nextPlayer()
  - getTilesRemaining(), isWallEmpty(), getVisibleDoraIndicators()
- Created comprehensive unit tests:
  - `WindTest`: Test wind rotation and tile conversion
  - `PlayerTest`: Test player state, hand size, melds, menzen detection
  - `GameStateTest`: Test game initialization, player rotation, wall tracking

#### Task: Implement wall/deck management
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `WallService` with comprehensive wall management:
  - initializeWall(): Creates full 136-tile set with shuffling
  - createFullTileSet(): Generates all tiles including 3 red 5s (one per suit)
  - setupDeadWall(): Reserves 14 tiles for dead wall
  - dealInitialHands(): Deals 13 tiles to each of 4 players
  - drawTileFromWall(): Draws tiles with wall exhaustion checking
  - drawReplacementTile(): Draws from dead wall for kan replacements
  - revealNextDoraIndicator(): Reveals additional dora (up to 5)
  - getActualDoraTiles(): Converts dora indicators to actual dora tiles
  - getNextTile(): Helper for dora rotation logic
- Dead wall structure:
  - 14 tiles reserved at end of wall
  - 5 dora indicators (positions 4, 3, 2, 1, 0)
  - 5 ura-dora indicators (positions 9, 8, 7, 6, 5)
  - Remaining tiles for kan replacements
- Logging throughout for debugging
- Created comprehensive unit tests in `WallServiceTest`:
  - Test wall initialization (136 tiles, dead wall, dora setup)
  - Test initial hand dealing (13 tiles per player)
  - Test tile drawing and wall exhaustion
  - Test replacement tile drawing
  - Test dora indicator revealing (up to 5 max)
  - Test red tile distribution (3 red 5s)
  - Test tile type distribution (4 of each type)

