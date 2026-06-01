# Mahjong Application - High-Level Design

## Project Overview
A Mahjong application that provides AI-powered move suggestions via API and includes a web-based visualization interface for viewing game states.

## Core Features

### 1. Move Suggestion API
- **REST API Endpoint**: Accept game state and return optimal move suggestions
- **Input**: JSON representation of current board/game state
- **Output**: Ranked list of suggested moves with confidence scores
- **AI Engine**: Intelligent move analysis based on Mahjong strategy

### 2. Web-Based Visualization
- **Localhost Interface**: Visual representation of game states
- **Real-time Display**: Show current board configuration, tiles, and game status
- **Interactive Features**: Highlight suggested moves, show tile details
- **Responsive Design**: Works on desktop and mobile browsers

## High-Level Architecture

### System Components

#### 1. **Core Game Engine**
- **Game State Manager**
  - Maintains current board state
  - Validates moves and game rules
  - Tracks player hands, discards, and available tiles
  
- **Rule Engine**
  - Implements Mahjong rules (which variant: Riichi/Japanese, Hong Kong, American, etc.?)
  - Validates legal moves
  - Detects winning conditions (Ron, Tsumo, etc.)
  - Calculates hand values and scoring

- **Tile Management**
  - Tile representation (suits, honors, winds, dragons)
  - Wall/deck management
  - Dora indicator handling

#### 2. **AI/Strategy Engine**
- **Move Analyzer**
  - Evaluates all possible moves from current state
  - Calculates hand efficiency (shanten number)
  - Considers tile probabilities and waits
  - Tracks visible tile counts from all players' discard piles to calculate accurate remaining-tile probabilities (ukeire weighting)
  - Reads tsumogiri (top-deck discard) vs tedashi (hand-chosen discard) flags to infer opponent hand shape and tenpai likelihood
  
- **Strategy Evaluator**
  - Defensive play analysis (safe tiles)
  - Offensive play optimization (fastest path to tenpai/winning)
  - Risk assessment for discards
  - Genbutsu detection: tiles already in an opponent's discard pile are guaranteed safe against ron from that player
  - Suji inference: use opponent discard patterns to identify likely-safe tiles
  - Tenpai danger assessment: consecutive tsumogiri discards from an opponent signal possible tenpai
  
- **Scoring Predictor**
  - Estimates potential hand value
  - Yaku (winning patterns) detection
  - Expected value calculations

#### 3. **REST API Layer**
- **Endpoints**
  - `POST /api/suggest-move`: Submit game state, receive move suggestions
  - `GET /api/game-state/{id}`: Retrieve stored game state
  - `POST /api/validate-move`: Validate if a move is legal
  - `GET /api/health`: Service health check
  
- **Request/Response Models**
  - Game state serialization (JSON)
  - Move representation format
  - Error handling and validation

#### 4. **Web Visualization Server**
- **Frontend Framework**: React/Vue.js or simple HTML/CSS/JS
- **Rendering Engine**
  - SVG or Canvas-based tile rendering
  - Board layout visualization
  - Animation for tile movements
  
- **Features**
  - Display submitted game state
  - Highlight suggested moves
  - Show hand analysis (shanten, waits, yaku potential)
  - Interactive tile inspection
  - Game history/replay functionality

#### 5. **Data Layer**
- **Database**: H2 (file-based mode)
  - Embedded database, zero external dependencies
  - Persists to local file for game history
  - Built-in web console for data inspection
  - Easy migration path to PostgreSQL if needed
  
- **ORM**: Spring Data JPA
  - Repository pattern for data access
  - Automatic CRUD operations
  - Entity mapping for game states and API call logs
  
- **Storage Requirements**
  - Game history (game states, moves, timestamps)
  - API call logs (request/response, performance metrics)
  - In-memory cache for active sessions (optional)
  
- **Configuration Management**
  - Rule variant settings
  - AI difficulty/strategy parameters
  - API rate limiting and quotas

## Technology Stack Considerations

### Backend
- **Language**: Java (Spring Boot for REST API)
- **Framework**: Spring MVC for web endpoints
- **AI/Logic**: Custom Java implementation or integration with existing Mahjong libraries
- **Serialization**: Jackson for JSON processing

### Frontend
- **Framework**: React or Vue.js (or vanilla JavaScript for simplicity)
- **Styling**: TailwindCSS or Bootstrap
- **Tile Graphics**: SVG assets or custom rendering
- **HTTP Client**: Axios or Fetch API

### Infrastructure
- **Server**: Embedded Tomcat (Spring Boot)
- **Port Configuration**: 
  - API: Port 8080
  - Visualization: Port 3000 or served from same Spring Boot app
- **Deployment**: Standalone JAR or Docker container

## Key Features Breakdown

### API Features
1. **Move Suggestion**
   - Accept complete game state (hands, discards, dora, round info)
   - Discard entries include `tsumogiri` flag per tile (see DiscardedTile model)
   - Return top N suggested moves with reasoning
   - Include confidence scores and strategic notes
   - Reasoning may reference opponent discard patterns (e.g., "tile is genbutsu safe vs. East", "reduced ukeire due to 2 copies visible in discards")

2. **Game State Validation**
   - Verify game state is legal
   - Check move validity
   - Provide error messages for invalid states

3. **Analysis Endpoint**
   - Detailed hand analysis
   - Shanten calculation
   - Winning tile probabilities
   - Yaku potential assessment

### Visualization Features
1. **Board Display**
   - Player hands (visible/hidden based on perspective)
   - Discard piles for all players
   - Dora indicators
   - Current player indicator
   - Round/wind information

2. **Move Highlighting**
   - Visual indication of suggested moves
   - Color-coded by recommendation strength
   - Hover tooltips with move reasoning

3. **Analysis Panel**
   - Hand efficiency metrics
   - Waiting tiles display
   - Potential yaku list
   - Score estimation

4. **Game State Input**
   - Form to manually input game state
   - Or paste JSON directly
   - Load from saved games

## Data Models

### Game State Structure
```
GameState:
  - gameId: string
  - variant: enum (Riichi, HongKong, etc.)
  - round: RoundInfo
  - players: Player[]
  - wall: TileCollection
  - doraIndicators: Tile[]
  - currentPlayer: int
  - lastAction: Action
```

### Player Structure
```
Player:
  - position: enum (East, South, West, North)
  - hand: Tile[]
  - discards: DiscardedTile[]
  - melds: Meld[]
  - riichi: boolean
  - score: int
```

### DiscardedTile Structure
```
DiscardedTile:
  - tile: Tile
  - tsumogiri: boolean   (true = discarded immediately on draw; false = held ≥1 turn before discarding)
```

> **Design Note**: The `tsumogiri` flag is visible in FF14 and enables two key inferences:
> 1. **Tile counting**: all discarded tiles are known and reduce the estimated draw probability for remaining copies in the wall.
> 2. **Hand-shape reading**: a run of tsumogiri discards from an opponent suggests they are in tenpai or close to it, raising the danger level of discarding into their wait.

### Move Suggestion Structure
```
MoveSuggestion:
  - move: Move
  - confidence: float (0-1)
  - reasoning: string
  - expectedValue: float
  - strategicType: enum (Offensive, Defensive, Balanced)
```

## Related Documentation

- **Development Plan**: See `dev-plan.md` for implementation phases, task tracking, and project decisions
- **Future Features**: See `future-features.md` for deferred enhancements and feature backlog
- **Tile Assets**: See `tile-assets.md` for frontend tile rendering documentation
