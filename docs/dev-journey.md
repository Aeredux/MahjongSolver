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

