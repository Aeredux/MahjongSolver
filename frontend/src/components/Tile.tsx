import { cn } from '@/lib/utils'
import { TILE_SPRITE_POSITIONS, TILE_DISPLAY_NAMES, type TileType } from '@/types/mahjong'

const SPRITE_COLS = 10
const SPRITE_ROWS = 4
const IMG_W = 2732          // full sprite sheet pixel width
const IMG_H = 871           // full sprite sheet pixel height
const TILE_W = 150          // tile content width per cell (px)
const TILE_H = 200          // tile content height per cell (px)
const CELL_W = IMG_W / SPRITE_COLS  // 273.2
const CELL_H = IMG_H / SPRITE_ROWS  // 217.75
const OFFSET_X = (CELL_W - TILE_W) / 2  // blue padding left of tile content in each cell
const OFFSET_Y = (CELL_H - TILE_H) / 2  // blue padding above tile content in each cell

interface TileProps {
  tile: TileType
  size?: 'sm' | 'md' | 'lg'
  highlighted?: boolean
  dimmed?: boolean
  onClick?: () => void
  onContextMenu?: (e: React.MouseEvent) => void
  className?: string
}

const SIZE_CLASSES = {
  sm: 'w-8 h-[43px]',
  md: 'w-10 h-[53px]',
  lg: 'w-12 h-16',
}

export function Tile({ tile, size = 'md', highlighted, dimmed, onClick, onContextMenu, className }: TileProps) {
  const pos = TILE_SPRITE_POSITIONS[tile]

  const bgSizeX = `${(IMG_W / TILE_W * 100).toFixed(2)}%`
  const bgSizeY = `${(IMG_H / TILE_H * 100).toFixed(2)}%`
  const bgPositionX = `${((pos.col * CELL_W + OFFSET_X) / (IMG_W - TILE_W) * 100).toFixed(4)}%`
  const bgPositionY = `${((pos.row * CELL_H + OFFSET_Y) / (IMG_H - TILE_H) * 100).toFixed(4)}%`

  return (
    <div
      role={onClick ? 'button' : undefined}
      title={TILE_DISPLAY_NAMES[tile]}
      onClick={onClick}
      onContextMenu={onContextMenu}
      className={cn(
        SIZE_CLASSES[size],
        'relative inline-block rounded-sm select-none flex-shrink-0',
        'border border-slate-600',
        highlighted && 'ring-2 ring-primary ring-offset-1 ring-offset-background',
        dimmed && 'opacity-40',
        onClick && 'cursor-pointer hover:scale-110 hover:ring-2 hover:ring-primary/60 hover:ring-offset-1 hover:ring-offset-background transition-transform',
        className
      )}
      style={{
        backgroundImage: "url('/assets/tiles/tiles.png')",
        backgroundSize: `${bgSizeX} ${bgSizeY}`,
        backgroundPosition: `${bgPositionX} ${bgPositionY}`,
        backgroundRepeat: 'no-repeat',
      }}
    />
  )
}
