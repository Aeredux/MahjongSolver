# Development Journey

This document tracks the development progress, decisions, and changes made during implementation.

## 2026-03-27

### DTO Update: Accept discards, seatWind, roundWind in evaluate-call
**Status**: Complete
**Date**: 2026-03-27

**Changes**:
- Added three new optional fields to `CallDecisionRequest`:
  - `opponents`: `List<PlayerDiscardsDTO>` — reuses existing DTO from `HandRequest`
  - `seatWind`: `Wind` — player's seat wind
  - `roundWind`: `Wind` — current round wind
- These fields are accepted and deserialized but **not yet used** in call decision logic (future enhancement)
- All existing tests pass; no breaking changes

### Global snake_case JSON naming
**Status**: Complete
**Date**: 2026-03-27

**Changes**:
- Added `spring.jackson.property-naming-strategy=SNAKE_CASE` to `application.properties`
- All JSON request/response field names are now snake_case (e.g. `called_tile`, `call_type`, `drawn_tile`, `current_shanten`, `discard_tile`, `shanten_after_discard`, `ukeire_count`, `should_call`, `shanten_before`, `shanten_after`, `player_score`, `open_kan`, `seat_wind`, `round_wind`)
- Renamed `CallDecisionRequest` fields `isMenzen` → `menzen`, `isOpenKan` → `openKan` to avoid Lombok boolean `is`-prefix causing Jackson to strip the prefix (JSON fields: `menzen`, `open_kan`)
- Updated `MahjongControllerTest` jsonPath assertions to match new field names
- Updated frontend TypeScript interfaces (`mahjong.ts`) and component property access (`MoveSuggestions.tsx`, `GameHistory.tsx`, `App.tsx`) to use snake_case
- Updated `api-guide.html` — all field names, examples, and docs now use snake_case; also documented the new `opponents`, `seat_wind`, `round_wind` fields for evaluate-call
- Rebuilt frontend (`npm run build`)
- All 113 backend tests pass; frontend builds cleanly

### Standardize hand size convention (always 13 tiles)
**Status**: Complete
**Date**: 2026-03-27

**Changes**:
- **Controller**: For TSUMO calls, controller now adds `called_tile` to hand internally before evaluating (13→14 tiles)
- **Controller**: For closed KAN calls, controller adds `called_tile` to hand internally when `open_kan=false` (13→14 tiles)
- **API docs**: Added clear "Hand Size Convention" section specifying `hand` = always 13 tiles, drawn/called tile always separate
- Updated all endpoint descriptions in `api-guide.html` to reflect the 13-tile convention
- All existing tests already used 13 tiles; no test changes needed
- All 113 backend tests pass

---

## 2026-03-24

### Bug Fix: Shanten and Ukeire Calculation (Phase 6 debugging)
**Status**: Fixed
**Date**: 2026-03-24

Three inter-related bugs found while testing the hand S1-S9 + M2M3 + WWW:

#### Bug 1: No-pair tatsu cap (3 melds + 2 tatsu + no pair = 0-shanten incorrectly)
**Root Cause**: `calculateShantenWithoutPair` and the `hasPair=false` base case in `calculateMeldFormation` both applied the formula `max(0, 8 - 2*melds - tatsu)` using the raw `tatsu` value, even when `melds + tatsu` exceeded the 4-group limit. For a hand with 3 complete melds + 2 partial sequences + no pair, the formula gave `max(0, 8-6-2) = 0` instead of the correct 1.

**Fix**: Cap tatsu at `min(tatsu, 4 - melds)` before applying the formula in both early-return sites. A standard win requires 4 melds + 1 pair; excess partial sequences beyond 4 total groups cannot be used.

**Test updates**: `testOneShantenHand` and `testTwoShantenHand` had expected values of 0 that relied on the bug — corrected to 1. Added regression test `testThreeMeldsNoPairIsOneShantenNotTenpai` covering the user-reported hand.

#### Bug 2b: Ukeire overcounting (~123) due to uncapped tatsu in `calculateShantenWithPair`
**Root Cause**: After the ryanmen fix (Bug 2), paths like 3 melds + 2 tatsu (e.g., WWW meld + S8S9 ryanmen + M2M3 ryanmen) now resolved to `8-6-2-1 = -1` in the `hasPair=true` formula sites in `calculateShantenWithPair`. The pair-candidate path in `calculateStandardShanten` would then return `-1+1 = 0` for almost every drawn tile, counting it as ukeire. Maximum possible ukeire for a 13-tile hand is `136 - 13 = 123`, which is exactly what users saw.

**Fix**: Added `effectiveTatsu = Math.min(tatsu, 4 - melds)` cap to both the empty-tiles base case and the `melds+tatsu >= 4` early-return in `calculateShantenWithPair`, mirroring the same cap already applied in `calculateShantenWithoutPair`.

**Result**: All 113 tests pass. Ukeire values are now realistic (e.g., ~4-8 for tenpai hands, ~20-40 for 1-shanten hands).

#### Bug 2: Ryanmen not detected for value-8 tiles (S8S9, P8P9, M8M9)
**Root Cause**: The sequence-forming block in `calculateMeldFormation` was gated by `firstType.getValue() <= 7`, which blocked ryanmen detection for S8S9 (S8 starts at value 8). After discarding S7, the S8S9 stub was silently discarded as two isolated tiles instead of being counted as a tatsu. This caused discard-S7 to show worse (and asymmetric) ukeire compared to the mirror discard-S3.

**Fix**: Moved the ryanmen check outside the `value <= 7` gate (ryanmen only needs +1, valid up to value 8). Complete sequence and kanchan remain gated at `value <= 7` (both need +2).

**Result**: All 113 tests pass. S3 and S7 discards now produce symmetric ukeire counts as expected.

#### Tile sprite padding (frontend)
**Root Cause**: The sprite sheet CSS used the formula `bgSize = NCOLS*100% / NROWS*100%` assuming zero padding, but each cell has ~8% blue border on each side from the shadow/frame art. Tiles appeared smaller than needed.

**Fix**: Added `TILE_PAD = 0.08` constant in `Tile.tsx` and applied corrected formulas:
- `bgSizeX = NCOLS / (1 - 2*P) * 100%`
- `bgPosX = 100 * (col + P) / (NCOLS - 1 + 2*P) %` (derived from CSS background-position percentage math)

---

### Feature: Discard-Aware Tile Efficiency, Genbutsu, Tsumogiri (Phase 2)
**Status**: Implemented 2026-03-24

**Overview**: Extended the design to incorporate visible opponent discard information into tile efficiency calculations, leveraging data exposed by the FF14 mahjong interface.

**Changes to `application-design.md`**:
- Updated `Player.discards` field type from `Tile[]` to `DiscardedTile[]`
- Added new `DiscardedTile` data model with fields: `tile: Tile` and `tsumogiri: boolean`
  - `tsumogiri = true`: tile was discarded immediately upon drawing (top-deck discard)
  - `tsumogiri = false`: tile was held for at least one turn before discarding (tedashi)
- Extended **Move Analyzer** to describe discard-based ukeire weighting and tsumogiri pattern reading
- Extended **Strategy Evaluator** to describe genbutsu detection, suji inference, and tenpai danger assessment
- Updated **API Features / Move Suggestion** to note that `DiscardedTile[]` is the expected input format and that reasoning may reference opponent discard patterns

**New files**:
- `dto/DiscardedTileDTO.java` — `{ TileType tile, boolean tsumogiri }`
- `dto/PlayerDiscardsDTO.java` — `{ Wind wind, List<DiscardedTileDTO> discards, boolean riichi }`
- `dto/ValidateMoveRequest.java` — `{ List<TileType> hand, TileType discardTile, TileType drawnTile, boolean riichi }`
- `config/RateLimitInterceptor.java` — per-IP sliding window rate limiter (stdlib only, no extra deps)
- `config/WebConfig.java` — registers rate limiter on `/api/**`; limit values from `api.rate-limit.*` properties

**Updated files**:
- `dto/HandRequest.java` — added `List<PlayerDiscardsDTO> opponents` (optional, null-safe)
- `service/MoveSuggestionService.java`:
  - New overload `suggestMoves(hand, opponents)`; old single-arg version delegates to it
  - `buildVisibleCounts()` — aggregates all opponent discards into a `Map<TileType, Integer>`
  - `buildGenbutsuMap()` — for each riichi opponent, collects their full discard pile as a safe-tile set
  - `buildTenpaiDanger()` — counts consecutive trailing tsumogiri per opponent (2+ = possible tenpai, 4+ = high danger)
  - `calculateUkeire()` — now subtracts `visibleCounts` from the 4-copy pool so ukeire reflects true wall contents
  - `generateReasoning()` — adds: adjusted wall-% ukeire, scarcity warnings (≤1 copy left), genbutsu labels, tsumogiri caution lines
- `controller/MahjongController.java`:
  - `GET /api/game-state/{id}` — returns `GameHistory` by ID or 404
  - `POST /api/validate-move` — validates tile-in-hand, hand size 13/14, riichi tsumogiri rule; returns `{ valid, errors[] }`
  - `POST /api/suggest-move` now passes `request.getOpponents()` to the service
- `static/api-guide.html` — documented all new fields: `opponents[]`, `DiscardedTile`, new endpoints, rate limit note, full request examples
- `src/test/resources/application-test.properties` — overrides rate limit to 1000/sec so integration tests pass
- `test/controller/MahjongControllerTest.java` — added `@ActiveProfiles("test")`

**Changes to `dev-plan.md`**:
- Phase 2 status → Complete; all discard-aware subtasks checked
- Phase 4 status → Complete; all deferred endpoints and rate limiting checked

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

