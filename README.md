<img width="1085" height="838" alt="image" src="https://github.com/user-attachments/assets/ced7a935-0a9f-497c-b772-be4736ae1170" />

# Mahjong AI Server

A REST API server providing AI-powered move suggestions and call decisions for Japanese Riichi Mahjong, designed for integration with FF14 Doman Mahjong plugin, including [MahjongHelper](https://github.com/Aeredux/MahjongHelper).

## Features

- **Move Suggestions**: Get ranked tile discard recommendations based on shanten minimization
- **Call Decisions**: Evaluate whether to call pon/chi/kan/ron/riichi
- **AI Algorithm**: Basic shanten calculation with ukeire (useful tile) counting
- **REST API**: Simple JSON-based API for easy integration
- **Comprehensive Testing**: Full unit and integration test coverage

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Server

```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

### API Documentation

Once running, visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## API Endpoints

### 1. Move Suggestion

**Endpoint**: `POST /api/suggest-move`

**Description**: Returns ranked list of tile discard suggestions.

**Request**:
```json
{
  "hand": ["M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "P1", "P1", "P1", "S5"],
  "drawnTile": "S6"
}
```

**Response**:
```json
{
  "currentShanten": 0,
  "suggestions": [
    {
      "discardTile": "S6",
      "shantenAfterDiscard": 0,
      "confidence": 1.0,
      "reasoning": "Maintains shanten at 0. Ukeire: 4 tiles (2.9% of remaining tiles).",
      "ukeireCount": 4
    },
    {
      "discardTile": "S5",
      "shantenAfterDiscard": 0,
      "confidence": 1.0,
      "reasoning": "Maintains shanten at 0. Ukeire: 4 tiles (2.9% of remaining tiles).",
      "ukeireCount": 4
    }
  ]
}
```

### 2. Call Decision

**Endpoint**: `POST /api/evaluate-call`

**Description**: Evaluates whether to call pon/chi/kan/ron/riichi.

**Request (Ron)**:
```json
{
  "hand": ["M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "P1", "P1", "P1", "S5"],
  "calledTile": "S5",
  "callType": "RON"
}
```

**Request (Riichi)**:
```json
{
  "hand": ["M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "P1", "P1", "P1", "S5"],
  "callType": "RIICHI",
  "isMenzen": true,
  "playerScore": 25000
}
```

**Request (Pon)**:
```json
{
  "hand": ["M1", "M2", "M3", "M4", "M5", "M6", "P5", "P5", "S1", "S2", "S3", "EAST", "SOUTH"],
  "calledTile": "P5",
  "callType": "PON"
}
```

**Request (Chi)**:
```json
{
  "hand": ["M1", "M2", "M3", "M4", "M5", "P1", "P2", "S1", "S2", "S3", "EAST", "SOUTH", "WEST"],
  "calledTile": "P3",
  "callType": "CHI",
  "sequenceTiles": ["P1", "P2"]
}
```

**Response**:
```json
{
  "callType": "RON",
  "shouldCall": true,
  "confidence": 1.0,
  "reasoning": "Winning hand detected - call RON",
  "shantenBefore": 0,
  "shantenAfter": 0
}
```

### 3. Health Check

**Endpoint**: `GET /api/health`

**Response**: `"Mahjong AI service is running"`

## Tile Notation

Tiles are represented using the following notation:

- **Man (Characters)**: M1-M9
- **Pin (Circles)**: P1-P9
- **Sou (Bamboo)**: S1-S9
- **Winds**: EAST, SOUTH, WEST, NORTH
- **Dragons**: WHITE, GREEN, RED

## Call Types

- `RON`: Win by calling opponent's discard
- `TSUMO`: Win by self-draw
- `RIICHI`: Declare ready hand
- `PON`: Call triplet
- `CHI`: Call sequence
- `KAN`: Call quad

## AI Strategy

The AI uses **shanten minimization** as its core strategy:

1. **Shanten Calculation**: Determines how many tiles away from tenpai (ready to win)
2. **Ukeire Counting**: Counts useful tiles that improve the hand
3. **Move Ranking**: Prioritizes moves that reduce shanten, then maximize ukeire
4. **Call Evaluation**: Recommends calls based on shanten impact and hand strength

## Development

### Running Tests

```bash
mvn test
```

### Building

```bash
mvn clean package
```

The JAR file will be created in `target/mahjong-server-0.1.0-SNAPSHOT.jar`

### H2 Database Console

Access the H2 console at `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:file:./data/mahjong`
- Username: `sa`
- Password: (leave empty)

## Project Structure

```
src/main/java/com/mahjong/
├── controller/       # REST API controllers
├── dto/             # Data Transfer Objects
├── model/           # Domain models (Tile, GameState, etc.)
└── service/         # Business logic (AI, calculations)
    ├── HandAnalyzer.java
    ├── ShantenCalculator.java
    ├── MoveSuggestionService.java
    ├── CallDecisionService.java
    └── WallService.java
```

## Documentation

- **Design Document**: `docs/application-design.md`
- **Development Plan**: `docs/dev-plan.md`
- **Development Journey**: `docs/dev-journey.md`
- **Future Features**: `docs/future-features.md`
- **Tile Assets**: `docs/tile-assets.md`

## License

This project is for personal use with FF14 Doman Mahjong.

## Contributing

This is a personal project for FF14 plugin integration. See `docs/future-features.md` for planned enhancements and [MahjongHelper](https://github.com/Aeredux/MahjongHelper) for the intended plugin integration target.
