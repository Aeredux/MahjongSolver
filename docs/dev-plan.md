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
**Status**: In Progress

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
**Status**: Not Started

**Tasks**:
- [ ] Implement shanten calculation algorithm
- [ ] Build move evaluation system
  - [ ] Evaluate all possible discards from current hand
  - [ ] Calculate hand efficiency for each option
  - [ ] Rank moves by shanten reduction
- [ ] Create tile probability tracking
- [ ] Implement waiting tile (machi) detection
- [ ] Test with known game scenarios and edge cases
- [ ] Performance optimization for move calculation

**Dependencies**: Phase 1 (Core Game Engine)

**Estimated Completion**: TBD

---

### Phase 3: Database & Persistence
**Status**: Not Started

**Tasks**:
- [ ] Configure H2 database (file-based mode)
- [ ] Set up Spring Data JPA
- [ ] Create entity models
  - [ ] GameHistory entity
  - [ ] ApiCallLog entity
- [ ] Implement repository interfaces
- [ ] Add database initialization scripts
- [ ] Configure H2 web console for debugging
- [ ] Test data persistence and retrieval

**Dependencies**: Phase 1 (for entity models)

**Estimated Completion**: TBD

---

### Phase 4: REST API
**Status**: In Progress

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

**Dependencies**: Phase 2 (AI Engine) - Complete

**Estimated Completion**: TBD

---

### Phase 5: Web Visualization
**Status**: Not Started

**Tasks**:
- [ ] Choose frontend framework (React/Vue.js/vanilla JS)
- [ ] Set up frontend project structure
- [ ] Implement tile rendering system
  - [ ] Load and parse tile sprite sheet
  - [ ] Create tile component/rendering function
  - [ ] Implement tile mapping (code to sprite position)
- [ ] Build game state visualization
  - [ ] Player hand display
  - [ ] Discard pile rendering
  - [ ] Dora indicators
  - [ ] Round/wind information
- [ ] Create move suggestion display
  - [ ] Highlight suggested tiles
  - [ ] Show confidence scores
  - [ ] Display reasoning/analysis
- [ ] Implement game state input form
  - [ ] Manual input interface
  - [ ] JSON paste option
- [ ] Add game history viewer
- [ ] Responsive design and styling
- [ ] Integration with backend API

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

**Active Phase**: None (Planning complete, ready to start Phase 1)

**Next Steps**:
1. Initialize Spring Boot project
2. Set up project structure and dependencies
3. Begin implementing tile and game state models

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

---

## Blockers & Issues

None currently.

---

## Future Considerations

See `future-features.md` for deferred features and enhancements.
