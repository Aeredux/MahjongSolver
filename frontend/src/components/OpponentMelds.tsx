import { useState } from 'react'
import { X, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tile } from '@/components/Tile'
import { TILE_GROUPS, type TileType, type MeldType, type Opponent, type Meld } from '@/types/mahjong'
import { cn } from '@/lib/utils'

const OPPONENT_WINDS: Array<{ wind: 'EAST' | 'SOUTH' | 'WEST'; label: string }> = [
  { wind: 'EAST', label: 'EAST' },
  { wind: 'SOUTH', label: 'SOUTH' },
  { wind: 'WEST', label: 'WEST' },
]

const MELD_LABELS: Record<MeldType, string> = {
  CHI: 'Chi',
  PON: 'Pon',
  KAN_OPEN: 'Kan',
  KAN_ADDED: 'Kan+',
}

const MELD_TILE_COUNT: Record<MeldType, number> = {
  CHI: 3,
  PON: 3,
  KAN_OPEN: 4,
  KAN_ADDED: 4,
}

interface AddMeldState {
  opponentWind: 'EAST' | 'SOUTH' | 'WEST'
  meldType: MeldType
  selectedTiles: TileType[]
}

interface OpponentMeldsProps {
  opponents: Opponent[]
  onChange: (opponents: Opponent[]) => void
}

export function OpponentMelds({ opponents, onChange }: OpponentMeldsProps) {
  const [addingMeld, setAddingMeld] = useState<AddMeldState | null>(null)

  function startAddMeld(wind: 'EAST' | 'SOUTH' | 'WEST') {
    setAddingMeld({ opponentWind: wind, meldType: 'PON', selectedTiles: [] })
  }

  function cancelAddMeld() {
    setAddingMeld(null)
  }

  function setMeldType(type: MeldType) {
    if (addingMeld) {
      setAddingMeld({ ...addingMeld, meldType: type, selectedTiles: [] })
    }
  }

  function confirmMeld(wind: 'EAST' | 'SOUTH' | 'WEST', type: MeldType, tiles: TileType[]) {
    const newOpponents = opponents.map(opp =>
      opp.wind === wind ? { ...opp, melds: [...opp.melds, { type, tiles } as Meld] } : opp
    )
    onChange(newOpponents)
    setAddingMeld(null)
  }

  function handleTileSelect(tile: TileType) {
    if (!addingMeld) return
    const { meldType, opponentWind, selectedTiles } = addingMeld
    const targetCount = MELD_TILE_COUNT[meldType]

    if (meldType === 'PON' || meldType === 'KAN_OPEN' || meldType === 'KAN_ADDED') {
      confirmMeld(opponentWind, meldType, Array(targetCount).fill(tile))
    } else {
      const newTiles = [...selectedTiles, tile]
      if (newTiles.length >= targetCount) {
        confirmMeld(opponentWind, meldType, newTiles.slice(0, targetCount))
      } else {
        setAddingMeld({ ...addingMeld, selectedTiles: newTiles })
      }
    }
  }

  function removeMeld(wind: 'EAST' | 'SOUTH' | 'WEST', meldIndex: number) {
    const newOpponents = opponents.map(opp =>
      opp.wind === wind ? { ...opp, melds: opp.melds.filter((_, i) => i !== meldIndex) } : opp
    )
    onChange(newOpponents)
  }

  const totalMelds = opponents.reduce((sum, opp) => sum + opp.melds.length, 0)

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">
          Opponent Melds{' '}
          <span className="text-muted-foreground font-normal text-sm">({totalMelds})</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-xs text-muted-foreground">
          Track opponent open melds (Chi/Pon/Kan) to improve tile counting accuracy.
        </p>
        {OPPONENT_WINDS.map(({ wind, label }) => {
          const opponent = opponents.find(o => o.wind === wind)!
          const isAdding = addingMeld?.opponentWind === wind

          return (
            <div key={wind} className="space-y-2">
              <div className="flex items-start gap-2 flex-wrap">
                <span className="text-xs font-semibold text-muted-foreground w-11 shrink-0 pt-1">
                  {label}
                </span>
                <div className="flex items-center gap-1.5 flex-wrap flex-1">
                  {opponent.melds.map((meld, i) => (
                    <div
                      key={i}
                      className="relative group flex items-center gap-0.5 border border-border rounded px-1.5 py-0.5"
                    >
                      <span className="text-[10px] text-muted-foreground mr-0.5 shrink-0">
                        {MELD_LABELS[meld.type]}
                      </span>
                      {meld.tiles.map((tile, j) => (
                        <Tile key={j} tile={tile} size="sm" />
                      ))}
                      <button
                        onClick={() => removeMeld(wind, i)}
                        className="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full bg-destructive text-white hidden group-hover:flex items-center justify-center"
                      >
                        <X className="w-2.5 h-2.5" />
                      </button>
                    </div>
                  ))}
                  {!isAdding && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-6 px-2 text-xs"
                      onClick={() => startAddMeld(wind)}
                    >
                      <Plus className="w-3 h-3" />
                      Meld
                    </Button>
                  )}
                </div>
              </div>

              {isAdding && (
                <div className="ml-13 border border-border rounded-md p-2 space-y-2 bg-secondary/30">
                  <div className="flex gap-1 flex-wrap items-center">
                    {(['CHI', 'PON', 'KAN_OPEN', 'KAN_ADDED'] as MeldType[]).map(type => (
                      <button
                        key={type}
                        onClick={() => setMeldType(type)}
                        className={cn(
                          'px-2 py-0.5 text-xs rounded border transition-colors',
                          addingMeld?.meldType === type
                            ? 'bg-primary text-primary-foreground border-primary'
                            : 'border-border text-muted-foreground hover:text-foreground'
                        )}
                      >
                        {MELD_LABELS[type]}
                      </button>
                    ))}
                    <button
                      onClick={cancelAddMeld}
                      className="px-2 py-0.5 text-xs rounded border border-border text-muted-foreground hover:text-foreground ml-auto"
                    >
                      Cancel
                    </button>
                  </div>

                  {addingMeld?.meldType === 'CHI' && addingMeld.selectedTiles.length > 0 && (
                    <div className="flex items-center gap-1">
                      <span className="text-xs text-muted-foreground">Selected:</span>
                      {addingMeld.selectedTiles.map((t, i) => (
                        <Tile key={i} tile={t} size="sm" />
                      ))}
                      <span className="text-xs text-muted-foreground">
                        ({3 - addingMeld.selectedTiles.length} more)
                      </span>
                    </div>
                  )}

                  <p className="text-[10px] text-muted-foreground">
                    {addingMeld?.meldType === 'CHI'
                      ? `Click 3 tiles for the sequence (${addingMeld.selectedTiles.length}/3)`
                      : addingMeld?.meldType === 'PON'
                      ? 'Click any tile to add a pon (×3)'
                      : 'Click any tile to add a kan (×4)'}
                  </p>

                  <div className="space-y-1">
                    {TILE_GROUPS.map(group => (
                      <div key={group.label} className="flex flex-wrap gap-0.5">
                        {group.tiles.map(tile => (
                          <Tile
                            key={tile}
                            tile={tile}
                            size="sm"
                            onClick={() => handleTileSelect(tile)}
                            className="cursor-pointer hover:opacity-80"
                          />
                        ))}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </CardContent>
    </Card>
  )
}
