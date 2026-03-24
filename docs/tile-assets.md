# Tile Assets Documentation

## Overview
Tile graphics for rendering Mahjong tiles in the web visualization interface.

## Asset Files
Located in: `src/main/resources/static/assets/tiles/`

### 1. `tiles.png` - Main Tile Sprite Sheet
A sprite sheet containing all Mahjong tile faces for Japanese Riichi Mahjong.

**Layout** (10 columns × 4 rows):
- **Row 1 (Characters/萬子 - Manzu)**: 1-9 man, plus red 5 man
- **Row 2 (Circles/筒子 - Pinzu)**: 1-9 pin, plus red 5 pin  
- **Row 3 (Bamboo/索子 - Souzu)**: 1-9 sou, plus red 5 sou
- **Row 4 (Honors/字牌 - Jihai)**: East, South, West, North, White Dragon, Red Dragon, Green Dragon

**Tile Dimensions**: Each tile appears to be approximately equal width/height
**Total Tiles**: 37 unique tile faces (including red dora variants)

### 2. Edge/Border Tiles (for 3D rendering)
- **`tileTop.png`**: Top edge of tile (horizontal strip)
- **`tileSide.png`**: Bottom/side edge (horizontal strip)
- **`tileLeft.png`**: Left edge (vertical strip)
- **`tileRight.png`**: Right edge (vertical strip)

**Color**: Purple/violet edges for tile depth effect

## Usage in Frontend

### Sprite Sheet Parsing
The frontend will need to:
1. Load `tiles.png` as a sprite sheet
2. Calculate individual tile dimensions (width/height of one tile)
3. Use CSS `background-position` or Canvas `drawImage()` to extract specific tiles
4. Map tile codes to sprite positions

### Tile Mapping Example
```javascript
// Example tile position mapping
const tilePositions = {
  // Characters (Man)
  '1m': { row: 0, col: 0 },
  '2m': { row: 0, col: 1 },
  // ... etc
  '5mr': { row: 0, col: 5 }, // red 5 man
  
  // Circles (Pin)
  '1p': { row: 1, col: 0 },
  // ... etc
  '5pr': { row: 1, col: 5 }, // red 5 pin
  
  // Bamboo (Sou)
  '1s': { row: 2, col: 0 },
  // ... etc
  '5sr': { row: 2, col: 5 }, // red 5 sou
  
  // Honors
  'E': { row: 3, col: 0 },  // East
  'S': { row: 3, col: 1 },  // South
  'W': { row: 3, col: 2 },  // West
  'N': { row: 3, col: 3 },  // North
  'C': { row: 3, col: 4 },  // White (Chun/中)
  'F': { row: 3, col: 5 },  // Red (Hatsu/發) - appears blank in sheet
  'P': { row: 3, col: 6 },  // Green (Haku/白)
};
```

### 3D Tile Rendering
For isometric or 3D tile views:
1. Render main tile face from `tiles.png`
2. Add edge pieces using `tileTop.png`, `tileSide.png`, `tileLeft.png`, `tileRight.png`
3. Position edges to create depth effect

## Notes
- Red 5 tiles (aka-dora) are included in the sprite sheet
- Tile back/face-down representation may need to be added separately
- Consider creating a tile mapping configuration file for the frontend
- Assets are served statically from Spring Boot at `/assets/tiles/`

## TODO
- [ ] Measure exact pixel dimensions of individual tiles in sprite sheet
- [ ] Create tile back image for face-down tiles
- [ ] Document exact sprite coordinates for programmatic access
- [ ] Consider creating a JSON mapping file for tile positions
