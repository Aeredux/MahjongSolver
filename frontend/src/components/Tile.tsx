import { cn } from '@/lib/utils'
import { TILE_SPRITE_POSITIONS, TILE_DISPLAY_NAMES, type TileType } from '@/types/mahjong'

const SPRITE_COLS = 10
const SPRITE_ROWS = 4
const TILE_PAD = 0.08  // fraction of each sprite cell that is padding on each side

interface TileProps {
  tile: TileType
  size?: 'sm' | 'md' | 'lg'
  highlighted?: boolean
  dimmed?: boolean
  onClick?: () => void
  className?: string
}

const SIZE_CLASSES = {
  sm: 'w-8 h-11',
  md: 'w-10 h-14',
  lg: 'w-12 h-16',
}

export function Tile({ tile, size = 'md', highlighted, dimmed, onClick, className }: TileProps) {
  const pos = TILE_SPRITE_POSITIONS[tile]

  const bgSizeX = `${(SPRITE_COLS / (1 - 2 * TILE_PAD) * 100).toFixed(2)}%`
  const bgSizeY = `${(SPRITE_ROWS / (1 - 2 * TILE_PAD) * 100).toFixed(2)}%`
  const bgPositionX = `${(100 * (pos.col + TILE_PAD) / (SPRITE_COLS - 1 + 2 * TILE_PAD)).toFixed(4)}%`
  const bgPositionY = `${(100 * (pos.row + TILE_PAD) / (SPRITE_ROWS - 1 + 2 * TILE_PAD)).toFixed(4)}%`

  return (
    <div
      role={onClick ? 'button' : undefined}
      title={TILE_DISPLAY_NAMES[tile]}
      onClick={onClick}
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
