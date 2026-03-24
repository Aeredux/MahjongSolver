import { useState } from 'react'
import { X, Send, RotateCcw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tile } from '@/components/Tile'
import { TILE_GROUPS, TILE_DISPLAY_NAMES, type TileType } from '@/types/mahjong'
import { cn } from '@/lib/utils'

interface HandInputProps {
  onSubmit: (hand: TileType[], drawnTile: TileType | null) => void
  isLoading: boolean
}

const MAX_HAND = 14

export function HandInput({ onSubmit, isLoading }: HandInputProps) {
  const [hand, setHand] = useState<TileType[]>([])
  const [drawnTileIndex, setDrawnTileIndex] = useState<number | null>(null)

  function addTile(tile: TileType) {
    if (hand.length >= MAX_HAND) return
    const newHand = [...hand, tile]
    setHand(newHand)
  }

  function removeTile(index: number) {
    const newHand = hand.filter((_, i) => i !== index)
    setHand(newHand)
    if (drawnTileIndex === index) setDrawnTileIndex(null)
    else if (drawnTileIndex !== null && index < drawnTileIndex) setDrawnTileIndex(drawnTileIndex - 1)
  }

  function toggleDrawnTile(index: number) {
    setDrawnTileIndex(prev => (prev === index ? null : index))
  }

  function handleReset() {
    setHand([])
    setDrawnTileIndex(null)
  }

  function handleSubmit() {
    if (hand.length < 1) return
    const drawn = drawnTileIndex !== null ? hand[drawnTileIndex] : null
    const handWithoutDrawn = drawnTileIndex !== null ? hand.filter((_, i) => i !== drawnTileIndex) : hand
    onSubmit(handWithoutDrawn, drawn)
  }

  const tileCountByType: Partial<Record<TileType, number>> = {}
  hand.forEach(t => { tileCountByType[t] = (tileCountByType[t] ?? 0) + 1 })

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center justify-between">
            <span>Your Hand <span className="text-muted-foreground font-normal text-sm">({hand.length}/{MAX_HAND})</span></span>
            <Button variant="ghost" size="sm" onClick={handleReset} disabled={hand.length === 0}>
              <RotateCcw className="w-4 h-4" />
              Reset
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent>
          {hand.length === 0 ? (
            <p className="text-muted-foreground text-sm">Click tiles below to build your hand.</p>
          ) : (
            <div className="space-y-2">
              <div className="flex flex-wrap gap-1.5">
                {hand.map((tile, i) => (
                  <div key={i} className="relative group flex flex-col items-center gap-0.5">
                    <Tile
                      tile={tile}
                      size="md"
                      highlighted={drawnTileIndex === i}
                      onClick={() => toggleDrawnTile(i)}
                      className={cn(drawnTileIndex === i && 'ring-2 ring-primary')}
                    />
                    <button
                      onClick={() => removeTile(i)}
                      className="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full bg-destructive text-white text-xs hidden group-hover:flex items-center justify-center"
                    >
                      <X className="w-2.5 h-2.5" />
                    </button>
                    {drawnTileIndex === i && (
                      <span className="text-[10px] text-primary font-semibold leading-none">draw</span>
                    )}
                  </div>
                ))}
              </div>
              <p className="text-xs text-muted-foreground">
                Click a tile to mark it as your drawn tile. Hover to remove.
                {drawnTileIndex !== null && (
                  <span className="text-primary ml-1">
                    Drawn: {TILE_DISPLAY_NAMES[hand[drawnTileIndex]]}
                  </span>
                )}
              </p>
            </div>
          )}
          <div className="mt-4 flex gap-2">
            <Button
              onClick={handleSubmit}
              disabled={hand.length < 1 || isLoading}
              className="flex-1"
            >
              <Send className="w-4 h-4" />
              {isLoading ? 'Analysing…' : 'Get Suggestions'}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Tile Picker</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {TILE_GROUPS.map(group => (
            <div key={group.label}>
              <p className="text-xs text-muted-foreground mb-1.5">{group.label}</p>
              <div className="flex flex-wrap gap-1">
                {group.tiles.map(tile => {
                  const count = tileCountByType[tile] ?? 0
                  const maxReached = count >= 4 || hand.length >= MAX_HAND
                  return (
                    <div key={tile} className="relative">
                      <Tile
                        tile={tile}
                        size="sm"
                        dimmed={maxReached}
                        onClick={maxReached ? undefined : () => addTile(tile)}
                      />
                      {count > 0 && (
                        <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-primary text-primary-foreground text-[10px] font-bold flex items-center justify-center">
                          {count}
                        </span>
                      )}
                    </div>
                  )
                })}
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
