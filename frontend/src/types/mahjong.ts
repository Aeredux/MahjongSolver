export type TileType =
  | 'M1' | 'M2' | 'M3' | 'M4' | 'M5' | 'M6' | 'M7' | 'M8' | 'M9'
  | 'P1' | 'P2' | 'P3' | 'P4' | 'P5' | 'P6' | 'P7' | 'P8' | 'P9'
  | 'S1' | 'S2' | 'S3' | 'S4' | 'S5' | 'S6' | 'S7' | 'S8' | 'S9'
  | 'EAST' | 'SOUTH' | 'WEST' | 'NORTH' | 'WHITE' | 'GREEN' | 'RED'

export interface TileSpritePosition {
  row: number
  col: number
}

export const TILE_SPRITE_POSITIONS: Record<TileType, TileSpritePosition> = {
  M1: { row: 0, col: 0 }, M2: { row: 0, col: 1 }, M3: { row: 0, col: 2 },
  M4: { row: 0, col: 3 }, M5: { row: 0, col: 4 }, M6: { row: 0, col: 6 },
  M7: { row: 0, col: 7 }, M8: { row: 0, col: 8 }, M9: { row: 0, col: 9 },
  P1: { row: 1, col: 0 }, P2: { row: 1, col: 1 }, P3: { row: 1, col: 2 },
  P4: { row: 1, col: 3 }, P5: { row: 1, col: 4 }, P6: { row: 1, col: 6 },
  P7: { row: 1, col: 7 }, P8: { row: 1, col: 8 }, P9: { row: 1, col: 9 },
  S1: { row: 2, col: 0 }, S2: { row: 2, col: 1 }, S3: { row: 2, col: 2 },
  S4: { row: 2, col: 3 }, S5: { row: 2, col: 4 }, S6: { row: 2, col: 6 },
  S7: { row: 2, col: 7 }, S8: { row: 2, col: 8 }, S9: { row: 2, col: 9 },
  EAST:  { row: 3, col: 0 }, SOUTH: { row: 3, col: 1 },
  WEST:  { row: 3, col: 2 }, NORTH: { row: 3, col: 3 },
  WHITE: { row: 3, col: 5 }, GREEN: { row: 3, col: 6 }, RED: { row: 3, col: 4 },
}

export const TILE_GROUPS: { label: string; tiles: TileType[] }[] = [
  {
    label: '萬子 Man',
    tiles: ['M1','M2','M3','M4','M5','M6','M7','M8','M9'],
  },
  {
    label: '筒子 Pin',
    tiles: ['P1','P2','P3','P4','P5','P6','P7','P8','P9'],
  },
  {
    label: '索子 Sou',
    tiles: ['S1','S2','S3','S4','S5','S6','S7','S8','S9'],
  },
  {
    label: '字牌 Honors',
    tiles: ['EAST','SOUTH','WEST','NORTH','WHITE','GREEN','RED'],
  },
]

export const TILE_DISPLAY_NAMES: Record<TileType, string> = {
  M1:'1m', M2:'2m', M3:'3m', M4:'4m', M5:'5m', M6:'6m', M7:'7m', M8:'8m', M9:'9m',
  P1:'1p', P2:'2p', P3:'3p', P4:'4p', P5:'5p', P6:'6p', P7:'7p', P8:'8p', P9:'9p',
  S1:'1s', S2:'2s', S3:'3s', S4:'4s', S5:'5s', S6:'6s', S7:'7s', S8:'8s', S9:'9s',
  EAST:'東', SOUTH:'南', WEST:'西', NORTH:'北',
  WHITE:'白', GREEN:'發', RED:'中',
}

export interface MoveSuggestion {
  discard_tile: TileType
  shanten_after_discard: number
  confidence: number
  reasoning: string
  ukeire_count: number
}

export interface MoveSuggestionResponse {
  suggestions: MoveSuggestion[]
  current_shanten: number
}

export interface GameHistoryEntry {
  id: number
  created_at: string
  hand_tiles: string
  drawn_tile: string | null
  current_shanten: number
  best_discard: string | null
  best_confidence: number
  suggestion_count: number
  discard_tiles: string | null
}
