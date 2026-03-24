# Development Journey

This document tracks the development progress, decisions, and changes made during implementation.

## 2026-03-24

### Design: Discard-Aware Tile Efficiency
**Status**: Documented (implementation deferred)

**Overview**: Extended the design to incorporate visible opponent discard information into tile efficiency calculations, leveraging data exposed by the FF14 mahjong interface.

**Changes to `application-design.md`**:
- Updated `Player.discards` field type from `Tile[]` to `DiscardedTile[]`
- Added new `DiscardedTile` data model with fields: `tile: Tile` and `tsumogiri: boolean`
  - `tsumogiri = true`: tile was discarded immediately upon drawing (top-deck discard)
  - `tsumogiri = false`: tile was held for at least one turn before discarding (tedashi)
- Extended **Move Analyzer** to describe discard-based ukeire weighting and tsumogiri pattern reading
- Extended **Strategy Evaluator** to describe genbutsu detection, suji inference, and tenpai danger assessment
- Updated **API Features / Move Suggestion** to note that `DiscardedTile[]` is the expected input format and that reasoning may reference opponent discard patterns

**Changes to `dev-plan.md`**:
- Replaced vague "Tile probability tracking (deferred)" task in Phase 2 with a detailed "Discard-aware tile efficiency (deferred)" task list covering: model update, tile counting, genbutsu detection, tsumogiri pattern reading, and reasoning output
- Added Phase 4 task to update the `POST /api/suggest-move` DTO to accept `DiscardedTile[]` per player
- Added decision log entry for 2026-03-24

### Phase 5: Web Visualization (React Frontend)
**Status**: Complete
**Date**: 2026-03-24

**Decisions**:
- Chose React (over Vue) for better TypeScript support, larger ecosystem, and shadcn/ui compatibility
- Used Vite as build tool (fast HMR, ESM-first, minimal config)
- Node.js installed via `winget` (v24.14.0)
- Build output directed to `src/main/resources/static` so Spring Boot serves the app at `localhost:8080`
- During dev: Vite dev server at `localhost:3000` proxies `/api` calls to Spring Boot at `localhost:8080`

**Frontend structure** (`frontend/`):
- `src/types/mahjong.ts` — All TypeScript types: `TileType`, `TILE_SPRITE_POSITIONS` (sprite sheet coords), `TILE_GROUPS` (suit groupings), `TILE_DISPLAY_NAMES`, `MoveSuggestionResponse`, `GameHistoryEntry`
- `src/lib/utils.ts` — `cn()` utility using `clsx` + `tailwind-merge`
- `src/components/ui/` — lightweight shadcn-style components: `Button`, `Card`, `Badge`
- `src/components/Tile.tsx` — renders a single tile from the 10×4 sprite sheet using CSS `background-position` percentages (works at any resolution)
- `src/components/TileRow.tsx` — renders a sequence of tiles with optional highlight
- `src/components/HandInput.tsx` — interactive tile picker grouped by suit; click to add, hover to remove, click again to mark drawn tile; per-tile count badge (max 4); Reset and Get Suggestions buttons
- `src/components/MoveSuggestions.tsx` — ranked suggestion list; top suggestion highlighted with trophy; confidence progress bar (color-coded green/amber/orange/gray); ukeire count; reasoning text; shanten badge
- `src/components/GameHistory.tsx` — fetches `GET /api/history`, shows all past requests with hand, drawn tile, best discard, confidence, and timestamp; Refresh button
- `src/App.tsx` — two-tab layout (Suggest Move / History); two-column grid on large screens; sticky header

**Backend addition**:
- Added `GET /api/history` endpoint to `MahjongController` returning all `GameHistory` records sorted by most recent

**Tile sprite mapping**:
- Sprite sheet is 10 cols × 4 rows: Man (row 0), Pin (row 1), Sou (row 2), Honors (row 3)
- 5m/5p/5s at col 4; red variants at col 5 (mapped but not in TileType enum yet); 6-9 at cols 6-9
- EAST=col0, SOUTH=col1, WEST=col2, NORTH=col3, WHITE=col4, GREEN=col5, RED=col6

**Build**: `npm run build` → outputs to `src/main/resources/static/` (index.html + assets); 79KB gzipped JS

---

### Phase 3: Database & Persistence
**Status**: Complete
**Date**: 2026-03-24

**Changes**:
- Created `entity` package with two JPA entities:
  - `GameHistory`: stores each move suggestion request — hand tiles (comma-separated), drawn tile, current shanten, best discard tile name, best confidence, suggestion count, and timestamp
  - `ApiCallLog`: stores every API call — endpoint, HTTP method, request body (TEXT column), response status, duration in ms, error message, and timestamp
- Both entities use `@PrePersist` to auto-set `createdAt` on first save
- Created `repository` package with Spring Data JPA interfaces:
  - `GameHistoryRepository`: `findAllByOrderByCreatedAtDesc()`
  - `ApiCallLogRepository`: `findAllByOrderByCreatedAtDesc()`, `findByEndpointOrderByCreatedAtDesc(String)`
- Created `GameHistoryService`: `saveGameHistory()`, `findAll()`, `findRecent(int)` (via `PageRequest`), `findById()`
- Created `ApiCallLogService`: `log()`, `findAll()`, `findByEndpoint()`, `findById()`
- Wired both services into `MahjongController`:
  - All POST endpoints time the request and log to `ApiCallLog` (success and error paths)
  - `/api/suggest-move` additionally saves a `GameHistory` record per request
  - Added `toJson()` helper for safe request serialization
- Tables are auto-created by `spring.jpa.hibernate.ddl-auto=update`; H2 console available at `/h2-console`
- Written tests:
  - `GameHistoryServiceTest`: 8 tests covering save, null drawn tile, empty suggestions, findById, findAll, findRecent limit, serialization format, best discard storage
  - `ApiCallLogServiceTest`: 7 tests covering log, error message, findById, findAll, findByEndpoint filtering, null request body, ordering
- All 112 tests pass

---

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

#### Task: Implement hand analysis utilities
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `HandAnalyzer` service with comprehensive hand analysis:
  - sortHand(): Sorts tiles by suit and value
  - getTileCounts(): Counts occurrences of each tile type
  - groupBySuit(): Groups tiles by suit for analysis
  - isPair(), isSequence(), isTriplet(), isQuad(): Meld validation
  - isWinningHand(): Detects winning hands (standard, seven pairs, thirteen orphans)
  - isSevenPairs(): Validates seven pairs (chiitoitsu) pattern
  - isThirteenOrphans(): Validates thirteen orphans (kokushi musou) pattern
  - isStandardForm(): Validates 4 melds + 1 pair structure
  - canFormMelds(): Recursive meld formation checker
  - getWaitingTiles(): Finds all tiles that complete a tenpai hand
  - countTilesInHand(): Counts specific tile types in hand
  - getNextTileType(): Helper for tile sequence logic
- Winning hand detection supports:
  - Standard form (4 melds + 1 pair)
  - Seven pairs (chiitoitsu)
  - Thirteen orphans (kokushi musou)
- Created comprehensive unit tests in `HandAnalyzerTest`:
  - Test hand sorting
  - Test tile counting and grouping
  - Test meld validation (pair, sequence, triplet, quad)
  - Test seven pairs detection
  - Test thirteen orphans detection
  - Test standard winning hand detection
  - Test non-winning hand rejection
  - Test waiting tile calculation
  - Test tile counting in hand

#### Task: Implement shanten calculation
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `ShantenCalculator` service for AI move evaluation:
  - calculateShanten(): Main entry point, returns minimum shanten across all patterns
  - calculateStandardShanten(): Standard 4 melds + 1 pair shanten calculation
  - calculateShantenWithoutPair(): Recursive algorithm for meld/tatsu counting
  - calculateChiitoitsuShanten(): Seven pairs pattern shanten (6 - pairs)
  - calculateKokushiShanten(): Thirteen orphans pattern shanten
  - calculateShantenAfterDiscard(): Evaluates shanten after discarding a tile
  - isTenpai(): Checks if hand is ready to win (shanten = 0)
- Shanten algorithm features:
  - Considers melds (complete groups of 3/4 tiles)
  - Considers tatsu (incomplete groups: pairs, ryanmen, kanchan)
  - Evaluates all three winning patterns (standard, chiitoitsu, kokushi)
  - Returns minimum shanten value across all patterns
  - Handles edge cases (empty hand, invalid discards)
- Logging for debugging shanten calculations
- Created comprehensive unit tests in `ShantenCalculatorTest`:
  - Test complete hand (shanten = -1)
  - Test tenpai hands (shanten = 0)
  - Test 1-shanten, 2-shanten hands
  - Test chiitoitsu pattern (tenpai and 1-shanten)
  - Test kokushi pattern (tenpai and 1-shanten)
  - Test shanten after discard calculation
  - Test random hands
  - Test edge cases (empty hand, all pairs)
  - Test isTenpai() helper method

#### Task: Implement move suggestion service
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `MoveSuggestion` model class:
  - discardTile: Tile to discard
  - shantenAfterDiscard: Resulting shanten value
  - confidence: Confidence score (0.0-1.0)
  - reasoning: Human-readable explanation
  - ukeireCount: Number of useful tiles remaining
- Created `MoveSuggestionService` for AI move recommendations:
  - suggestMoves(): Generates all possible moves with rankings
  - getBestMove(): Returns single best move suggestion
  - getTopMoves(): Returns top N move suggestions
  - calculateUkeire(): Counts tiles that improve hand after discard
  - calculateConfidence(): Scores move quality (1.0 = improves shanten, 0.5 = maintains, 0.0 = worsens)
  - generateReasoning(): Creates human-readable explanation
  - explainMove(): Formats move suggestion as text
- Move ranking algorithm:
  - Primary: Lowest shanten after discard
  - Secondary: Highest ukeire count
  - Tertiary: Highest confidence score
- Ukeire calculation:
  - Tests all 34 tile types
  - Counts remaining tiles that improve shanten
  - Accounts for tiles already in hand
- Reasoning includes:
  - Shanten change (improves/maintains/worsens)
  - Ukeire count and percentage
  - Terminal/honor tile safety notes
- Created comprehensive unit tests in `MoveSuggestionServiceTest`:
  - Test move suggestions for tenpai hands
  - Test move suggestions for 1-shanten hands
  - Test getBestMove() functionality
  - Test getTopMoves() with limit
  - Test empty hand handling
  - Test reasoning generation
  - Test confidence scoring
  - Test move explanation formatting
  - Test suggestion sorting by quality
  - Test null hand handling

#### Task: Implement call decision service
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created `CallType` enum (CHI, PON, KAN, RON, RIICHI, TSUMO)
- Created `CallDecision` model class:
  - callType: Type of call being evaluated
  - shouldCall: Boolean recommendation
  - confidence: Confidence score (0.0-1.0)
  - reasoning: Human-readable explanation
  - shantenBefore/shantenAfter: Shanten change tracking
- Created `CallDecisionService` for evaluating call actions:
  - evaluateRon(): Checks if hand wins with called tile
  - evaluateTsumo(): Checks if hand is complete for self-draw win
  - evaluateRiichi(): Validates riichi conditions (menzen, tenpai, score >= 1000)
  - evaluatePon(): Evaluates pon based on shanten improvement
  - evaluateChi(): Evaluates chi based on shanten improvement and sequence validity
  - evaluateKan(): Evaluates kan based on hand strength (recommended at tenpai/1-shanten)
- Decision logic:
  - Ron/Tsumo: Always call if winning hand detected
  - Riichi: Call if hand is tenpai, closed, and has sufficient score
  - Pon/Chi: Recommend if maintains or improves shanten
  - Kan: Recommend if hand is close to winning (for dora bonus)
- Reasoning generation includes:
  - Validation checks (sufficient tiles, valid sequences, etc.)
  - Shanten impact analysis
  - Strategic considerations (menzen loss, dora opportunities)
- Created comprehensive unit tests in `CallDecisionServiceTest`:
  - Test ron with winning/non-winning hands
  - Test tsumo with winning hand
  - Test riichi with tenpai/non-tenpai hands
  - Test riichi validation (open hand, insufficient score)
  - Test pon with sufficient/insufficient tiles
  - Test chi with valid sequences and honor tiles
  - Test kan with sufficient/insufficient tiles

### Bug Fix: Shanten Calculation for Isolated Tiles
**Status**: Fixed
**Date**: 2026-03-23

**Problem**: `testExplainMove` revealed that discarding a meld tile from a hand with complete melds + isolated honor tiles (e.g., EAST/WEST) incorrectly showed 5-shanten instead of the expected 1-shanten.

**Root Cause 1** (`calculateMeldFormation`): The "discard isolated tile" code block had a guard condition `if (remainingTiles > 0)` that prevented using the computed shanten result when the last isolated tile was discarded. At that point `remainingTiles = 0`, so `minShanten` stayed at `MAX_SHANTEN = 8`. This caused the standard shanten path to return 8, allowing chiitoitsu (6 − 1 pair = 5-shanten) to dominate.

**Root Cause 2** (`calculateMeldFormation` / `calculateShantenWithoutPair`): With the above fix applied, a secondary issue emerged: for 14-tile hands the no-pair path could now form 4 melds + 1 tatsu = 14 tiles, making the formula `8 − 8 − 1 = −1` (spurious "complete hand"). Without a designated pair you can never reach −1, so clamping was needed.

**Fix**:
1. Removed the `if (remainingTiles > 0)` guard — isolated tiles at the tail of the hand are now always accounted for.
2. Applied `Math.max(0, ...)` to all no-pair (`hasPair=false`) formula return sites in `calculateMeldFormation` and `calculateShantenWithoutPair`, preventing a false −1 result on 14-tile hands.

**Test update** (`MoveSuggestionServiceTest.testExplainMove`): Replaced the TODO/bug-print workaround with a proper `assertEquals(1, ...)` assertion for every meld tile discard.

**Verified**: All 97 tests pass after the fix.

### Phase 4: REST API - Started

#### Task: Implement REST API endpoints
**Status**: Completed
**Started**: 2026-03-23
**Completed**: 2026-03-23

**Changes**:
- Created DTOs (Data Transfer Objects):
  - `HandRequest`: Request with hand tiles and optional drawn tile
  - `MoveSuggestionResponse`: Response with suggestions and current shanten
  - `CallDecisionRequest`: Request with hand, called tile, call type, and context
  - `CallDecisionResponse`: Response with call recommendation and reasoning
- Created `MahjongController` REST controller:
  - `POST /api/suggest-move`: Returns ranked move suggestions
  - `POST /api/evaluate-call`: Evaluates call decisions (pon/chi/kan/ron/riichi)
  - `GET /api/health`: Health check endpoint
- API features:
  - JSON request/response format
  - Comprehensive error handling
  - Logging for all requests
  - Swagger/OpenAPI documentation integration
  - Converts between DTOs and domain models
- Endpoint details:
  - `/api/suggest-move`: Accepts hand + drawn tile, returns all possible discards ranked by shanten
  - `/api/evaluate-call`: Accepts hand + call context, returns boolean recommendation with confidence
  - Both endpoints include reasoning text for transparency
- Created comprehensive integration tests in `MahjongControllerTest`:
  - Test health endpoint
  - Test move suggestion endpoint with valid hand
  - Test ron call evaluation (winning hand)
  - Test riichi call evaluation (tenpai hand)
  - Test pon call evaluation
  - Test chi call evaluation with sequence tiles
  - Test empty hand handling
  - All tests verify JSON response structure

