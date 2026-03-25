# Mahjong Server API Guide

Base URL: `http://localhost:8080`

This server provides Japanese Riichi Mahjong move suggestions and call-decision evaluation.
All requests and responses use JSON. All tile types are passed as strings (see Tile Reference below).

---

## Tile Reference

Tiles are represented as strings using the following codes:

### Suited Tiles
| Suit | Tiles |
|------|-------|
| Manzu (characters, 万) | `M1` `M2` `M3` `M4` `M5` `M6` `M7` `M8` `M9` |
| Pinzu (circles, 筒)    | `P1` `P2` `P3` `P4` `P5` `P6` `P7` `P8` `P9` |
| Souzu (bamboo, 索)     | `S1` `S2` `S3` `S4` `S5` `S6` `S7` `S8` `S9` |

### Honor Tiles
| Name | Code | Description |
|------|------|-------------|
| East wind  | `EAST`  | 東 |
| South wind | `SOUTH` | 南 |
| West wind  | `WEST`  | 西 |
| North wind | `NORTH` | 北 |
| White dragon | `WHITE` | 白 (Haku) |
| Green dragon | `GREEN` | 発 (Hatsu) |
| Red dragon   | `RED`   | 中 (Chun) |

---

## Endpoints

---

### GET /api/health

Health check. Returns `200 OK` with a plain string when the server is running.

**Response:**
```
"OK"
```

---

### POST /api/suggest-move

Given a player's current hand (13 or 14 tiles), returns a ranked list of discard suggestions
with shanten values, ukeire counts, confidence scores, and reasoning.

**Request body:**
```json
{
  "hand": ["<TileType>", ...],
  "drawnTile": "<TileType> or null"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `hand` | array of TileType strings | Yes | 13 or 14 tiles in the player's hand. If 14 tiles and `drawnTile` is set, the drawn tile should be included in `hand`. |
| `drawnTile` | TileType string or null | No | The tile the player just drew. Can be null. Used for display/context only — the suggestion engine evaluates all tiles in `hand`. |

**Response body:**
```json
{
  "currentShanten": 0,
  "suggestions": [
    {
      "discardTile": "WHITE",
      "shantenAfterDiscard": 0,
      "confidence": 1.0,
      "reasoning": "Discarding WHITE keeps you at tenpai (0-shanten). 8 tiles can improve your hand.",
      "ukeireCount": 8
    },
    {
      "discardTile": "S1",
      "shantenAfterDiscard": 1,
      "confidence": 0.5,
      "reasoning": "Discarding S1 worsens shanten from 0 to 1.",
      "ukeireCount": 24
    }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `currentShanten` | int | Shanten of the current hand. -1 = complete hand, 0 = tenpai, 1 = 1-shanten, etc. |
| `suggestions` | array | All possible discards, sorted best-first. |
| `suggestions[].discardTile` | TileType string | The tile to discard. |
| `suggestions[].shantenAfterDiscard` | int | Shanten after discarding this tile. |
| `suggestions[].confidence` | float 0.0–1.0 | 1.0 = improves/maintains best shanten; 0.5 = suboptimal; 0.0 = worsens shanten. |
| `suggestions[].reasoning` | string | Human-readable explanation of why this discard was ranked this way. |
| `suggestions[].ukeireCount` | int | Number of tiles remaining in the wall (out of 4 copies each) that would improve the hand after this discard. |

**Example request (14-tile hand with drawn tile):**
```json
{
  "hand": ["S1","S2","S3","S4","S5","S6","S7","S8","S9","M2","M3","WHITE","WHITE","WHITE"],
  "drawnTile": null
}
```

**Example request (13-tile hand, no drawn tile):**
```json
{
  "hand": ["M1","M2","M3","M4","M5","M6","P1","P2","P3","S7","S8","S9","EAST"],
  "drawnTile": null
}
```

---

### POST /api/evaluate-call

Evaluates whether the player should make a call (pon, chi, kan, ron, riichi, tsumo).

**Request body:**
```json
{
  "hand": ["<TileType>", ...],
  "calledTile": "<TileType>",
  "callType": "<CallType>",
  "sequenceTiles": ["<TileType>", "<TileType>"],
  "isMenzen": true,
  "playerScore": 25000,
  "isOpenKan": false
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `hand` | array of TileType strings | Yes | Current tiles in player's hand (not including the called tile). |
| `calledTile` | TileType string | Yes | The tile being considered for the call. |
| `callType` | string | Yes | One of: `CHI`, `PON`, `KAN`, `RON`, `RIICHI`, `TSUMO` |
| `sequenceTiles` | array of 2 TileType strings | For CHI only | The two tiles in hand that form a sequence with `calledTile`. E.g. if calling chi on `M3`, sequenceTiles might be `["M1","M2"]` or `["M4","M5"]`. |
| `isMenzen` | boolean | No | Whether the hand is currently closed (no open melds). Default: true. |
| `playerScore` | int | No | Player's current point score. Used for riichi validation (needs ≥1000). Default: 25000. |
| `isOpenKan` | boolean | No | Whether the kan would be open (called from another player). Default: false. |

**CallType values:**
- `RON` — Win by claiming another player's discard
- `TSUMO` — Win by self-draw
- `RIICHI` — Declare riichi (closed tenpai hand)
- `PON` — Call a tile to form a triplet
- `CHI` — Call a tile to form a sequence (left player only)
- `KAN` — Call or declare a quad

**Response body:**
```json
{
  "callType": "PON",
  "shouldCall": true,
  "confidence": 0.8,
  "reasoning": "Pon improves hand efficiency. Shanten goes from 2 to 1.",
  "shantenBefore": 2,
  "shantenAfter": 1
}
```

| Field | Type | Description |
|-------|------|-------------|
| `callType` | string | Echoes back the call type. |
| `shouldCall` | boolean | `true` = recommended to make the call. |
| `confidence` | float 0.0–1.0 | Confidence in the recommendation. |
| `reasoning` | string | Explanation. |
| `shantenBefore` | int | Shanten before the call. |
| `shantenAfter` | int | Shanten after the call (if made). |

**Example — evaluate ron:**
```json
{
  "hand": ["M1","M2","M3","M4","M5","M6","P1","P2","P3","S7","S8","S9","EAST"],
  "calledTile": "EAST",
  "callType": "RON",
  "isMenzen": true,
  "playerScore": 25000
}
```

**Example — evaluate chi:**
```json
{
  "hand": ["M3","M4","P1","P2","P3","S1","S2","S3","S7","S8","S9","EAST","EAST"],
  "calledTile": "M2",
  "callType": "CHI",
  "sequenceTiles": ["M3","M4"],
  "isMenzen": true,
  "playerScore": 25000
}
```

---

### GET /api/history

Returns all past move suggestion requests stored in the database, most recent first.

**Response body:**
```json
[
  {
    "id": 1,
    "handTiles": "S1,S2,S3,M2,M3,WHITE,WHITE,WHITE,S4,S5,S6,S7,S8,S9",
    "drawnTile": null,
    "currentShanten": 0,
    "bestDiscard": "WHITE",
    "bestConfidence": 1.0,
    "suggestionCount": 14,
    "createdAt": "2026-03-24T17:10:58"
  }
]
```

---

## Shanten Explained

| Value | Meaning |
|-------|---------|
| -1 | Complete hand (tenpai + winning tile in hand) |
| 0  | Tenpai — one tile away from winning |
| 1  | 1-shanten — one useful draw + discard away from tenpai |
| N  | N tiles away from tenpai |

## Confidence Score Explained

| Value | Meaning |
|-------|---------|
| 1.0 | Best discard — achieves the lowest possible shanten |
| 0.5 | Maintains shanten (not optimal but not harmful) |
| 0.0 | Worsens shanten |

## Integration Notes

- The server runs on `http://localhost:8080` by default.
- All tile types in requests are case-sensitive (use uppercase: `M1`, not `m1`).
- A 14-tile hand is typical after drawing; the API will evaluate which tile to discard.
- A 13-tile hand is also accepted (useful for pre-draw analysis).
- `drawnTile` is informational — include it in `hand` if you want it evaluated as a discard candidate.
- The `suggestions` array always contains every possible discard, sorted by quality (best first).
- Use `suggestions[0]` for the single best recommended discard.
