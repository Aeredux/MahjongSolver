# Development Plan & Task Tracking

## Project Decisions

### Confirmed Specifications
- **Mahjong Variant**: Japanese Riichi
- **AI Complexity**: Basic shanten minimization (advanced strategies deferred)
- **Multiplayer**: Not implemented initially
- **API Rate Limiting**: ~5 requests/second (adjust based on performance)
- **Persistence**: H2 database (file-based) for game history and API call logs
- **User Accounts**: Not implemented initially
- **Technology Stack**: Java + Spring Boot backend, React/Vue.js or vanilla JS frontend

### Success Metrics
- **API Performance**: Response time < 500ms for move suggestions
- **Accuracy**: AI suggestions align with expert play
- **Usability**: Clear, intuitive visualization interface
- **Reliability**: 99%+ uptime, robust error handling
- **Extensibility**: Easy to add new variants or features

### Testing Requirements
- **Unit Tests**: Write unit tests for all new features and core game logic
- **Test Coverage**: Aim for >80% code coverage on business logic
- **Test-Driven Development**: Consider writing tests before implementation for complex algorithms
- **Testing Framework**: JUnit 5 for Java backend, Jest/Vitest for frontend
- **Integration Tests**: Add integration tests for API endpoints
- **Edge Cases**: Always test edge cases and error conditions

## Development Phases

### Phase 1: Core Game Engine
**Status**: Complete

**Tasks**:
- [x] Set up Spring Boot project structure
- [x] Implement tile models (suits, honors, winds, dragons)
- [x] Create game state data structures
- [x] Build rule engine for Japanese Riichi Mahjong (basic AI complete)
  - [x] Hand analysis utilities (sorting, grouping, winning hand detection)
  - [x] Shanten calculation and move suggestion service
  - [x] Call decision logic (pon/chi/kan/ron/riichi evaluation)
  - [ ] Legal move validation (deferred - not needed for basic AI)
  - [ ] Winning condition detection (Ron, Tsumo) (deferred - not needed for basic AI)
  - [ ] Hand value calculation (deferred - advanced feature)
  - [ ] Yaku recognition (deferred - advanced feature)
- [x] Implement wall/deck management
- [x] Dora indicator handling
- [x] Unit tests for game rules and validation (basic AI components tested)

**Dependencies**: None

**Estimated Completion**: TBD

---

### Phase 2: AI/Strategy Engine
**Status**: Complete (implemented as part of Phase 1)

**Tasks**:
- [x] Implement shanten calculation algorithm (`ShantenCalculator`)
- [x] Build move evaluation system
  - [x] Evaluate all possible discards from current hand
  - [x] Calculate hand efficiency for each option
  - [x] Rank moves by shanten reduction
- [x] Implement waiting tile (machi) / ukeire detection
- [x] Test with known game scenarios and edge cases
- [ ] Discard-aware tile efficiency (deferred)
  - [ ] Add `tsumogiri` boolean flag to `DiscardedTile` model (top-deck discard vs held)
  - [ ] Update `GameStateRequest` DTO to accept `DiscardedTile[]` per player instead of `Tile[]`
  - [ ] Implement tile counting: subtract all visible discards from remaining-tile counts when calculating ukeire probabilities
  - [ ] Implement genbutsu detection: flag tiles present in an opponent's discard pile as guaranteed safe against that player's ron
  - [ ] Implement tsumogiri pattern reading: track consecutive tsumogiri discards per opponent to estimate tenpai danger level
  - [ ] Surface discard-based reasoning in `MoveSuggestion.reasoning` (e.g. "genbutsu safe vs East", "only 1 copy left in wall")
- [ ] Performance optimization (current performance meets <500ms target)

**Dependencies**: Phase 1 (Core Game Engine)

**Estimated Completion**: TBD

---

### Phase 3: Database & Persistence
**Status**: Complete

**Tasks**:
- [x] Configure H2 database (file-based mode)
- [x] Set up Spring Data JPA
- [x] Create entity models
  - [x] `GameHistory` entity (hand tiles, drawn tile, shanten, best discard, confidence, suggestion count, timestamp)
  - [x] `ApiCallLog` entity (endpoint, method, request body, status, duration, error, timestamp)
- [x] Implement repository interfaces (`GameHistoryRepository`, `ApiCallLogRepository`)
- [x] Add database initialization scripts (handled by `spring.jpa.hibernate.ddl-auto=update`)
- [x] Configure H2 web console for debugging (accessible at `/h2-console`)
- [x] Test data persistence and retrieval (`GameHistoryServiceTest`, `ApiCallLogServiceTest`)

**Dependencies**: Phase 1 (for entity models)

**Estimated Completion**: TBD

---

### Phase 4: REST API
**Status**: Mostly Complete (core endpoints done; deferred items depend on Phase 3)

**Tasks**:
- [x] Set up Spring Boot REST controllers
- [x] Implement core API endpoints:
  - [x] `POST /api/suggest-move` - Move suggestion endpoint
  - [x] `POST /api/evaluate-call` - Call decision endpoint (pon/chi/kan/ron/riichi)
  - [x] `GET /api/health` - Health check
  - [ ] `GET /api/game-state/{id}` - Retrieve game state (deferred)
  - [ ] `POST /api/validate-move` - Move validation (deferred)
  - [ ] `GET /api/history` - Game history retrieval (deferred)
- [x] Create request/response DTOs
- [x] Implement JSON serialization for game states
- [x] Add request validation and error handling
- [ ] Implement rate limiting (~5 req/sec) (deferred)
- [ ] Add API logging to database (deferred)
- [x] API documentation (Swagger/OpenAPI integration)
- [x] Integration tests for core endpoints
- [ ] Update `POST /api/suggest-move` request DTO: `discards` per player changed from `Tile[]` to `DiscardedTile[]` with `tsumogiri` flag (prerequisite for discard-aware efficiency)

**Dependencies**: Phase 2 (AI Engine) - Complete

**Estimated Completion**: TBD

---

### Phase 5: Web Visualization
**Status**: Complete

**Tasks**:
- [x] Choose frontend framework (React + Vite + TypeScript + TailwindCSS)
- [x] Set up frontend project structure (`frontend/` directory)
- [x] Implement tile rendering system
  - [x] CSS sprite sheet extraction (10×4 grid, percentage-based background-position)
  - [x] `Tile` component with size variants, highlight, and dimmed states
  - [x] `TileRow` component for rendering a sequence of tiles
  - [x] Tile mapping for all 34 types including honors (`TILE_SPRITE_POSITIONS`)
- [x] Build game state visualization
  - [x] Visual tile picker grouped by suit (萬/筒/索/字)
  - [x] Current hand display with drawn tile marker
- [x] Create move suggestion display
  - [x] Ranked suggestion list with top suggestion highlighted
  - [x] Confidence progress bar (color-coded)
  - [x] Ukeire count and reasoning text per suggestion
  - [x] Shanten badge (tenpai / N-shanten)
- [x] Implement game state input form
  - [x] Interactive tile picker (click to add, hover to remove)
  - [x] Drawn tile marking (click selected tile)
  - [x] Per-tile count indicator (max 4)
- [x] Add game history viewer (fetches `GET /api/history`)
- [x] Responsive design and styling (dark theme, TailwindCSS)
- [x] Integration with backend API (Vite proxy to Spring Boot during dev; static build for production)
- [x] Added `GET /api/history` endpoint to backend
- [x] Build output configured to `src/main/resources/static`

**Dependencies**: Phase 4 (REST API)

**Estimated Completion**: TBD

---

### Phase 6: Integration & Testing
**Status**: Not Started

**Tasks**:
- [ ] End-to-end testing
  - [ ] Test full workflow: input → API → suggestion → display
  - [ ] Test with FF14 plugin integration
- [ ] Performance testing and optimization
  - [ ] Measure API response times
  - [ ] Optimize shanten calculation if needed
  - [ ] Database query optimization
- [ ] Error handling improvements
- [ ] Edge case testing
- [ ] Load testing for rate limiting
- [ ] Documentation
  - [ ] API usage guide
  - [ ] Deployment instructions
  - [ ] FF14 plugin integration guide
- [ ] Deployment preparation
  - [ ] Build standalone JAR
  - [ ] Configuration externalization
  - [ ] Logging configuration

**Dependencies**: All previous phases

**Estimated Completion**: TBD

---

## Current Sprint

**Active Phase**: Phase 6 (Integration & Testing)

**Next Steps**:
1. End-to-end test the full workflow (build → Spring Boot → browser)
2. Test FF14 plugin integration
3. Performance and load testing
4. Documentation (API guide, deployment instructions)

---

## Completed Tasks

### Planning & Design
- [x] Create high-level application design
- [x] Document architecture and components
- [x] Make key technology decisions
- [x] Set up project documentation structure
- [x] Copy tile assets to project
- [x] Document tile asset structure

---

## Notes & Decisions Log

**2026-03-23**: Initial planning completed
- Confirmed Japanese Riichi as target variant
- Basic shanten-based AI for initial release
- H2 database selected for persistence
- Tile assets copied and documented
- Development phases defined

**2026-03-24**: Discard-aware tile efficiency feature designed
- FF14 exposes each player's discard pile with tsumogiri (top-deck discard) vs tedashi (held discard) distinction
- `Player.discards` model updated to `DiscardedTile[]`; each entry carries tile + `tsumogiri: boolean`
- Three capabilities planned: (1) tile counting for accurate ukeire weighting, (2) genbutsu safety detection, (3) tsumogiri pattern reading for opponent tenpai danger
- Implementation deferred until after Phase 3; API DTO update (`DiscardedTile[]`) is a prerequisite

---

## Blockers & Issues

None currently.

---

## Future Considerations

See `future-features.md` for deferred features and enhancements.
