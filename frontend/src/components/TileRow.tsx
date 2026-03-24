import { Tile } from '@/components/Tile'
import { type TileType } from '@/types/mahjong'
import { cn } from '@/lib/utils'

interface TileRowProps {
  tiles: TileType[]
  highlightedTile?: TileType | null
  size?: 'sm' | 'md' | 'lg'
  onTileClick?: (tile: TileType, index: number) => void
  className?: string
  label?: string
}

export function TileRow({ tiles, highlightedTile, size = 'md', onTileClick, className, label }: TileRowProps) {
  if (tiles.length === 0) return null

  return (
    <div className={cn('flex flex-col gap-1', className)}>
      {label && <span className="text-xs text-muted-foreground">{label}</span>}
      <div className="flex flex-wrap gap-1">
        {tiles.map((tile, i) => (
          <Tile
            key={`${tile}-${i}`}
            tile={tile}
            size={size}
            highlighted={highlightedTile === tile}
            onClick={onTileClick ? () => onTileClick(tile, i) : undefined}
          />
        ))}
      </div>
    </div>
  )
}
