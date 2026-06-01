import { useEffect, useState } from 'react'
import { Clock, RefreshCw, Upload } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { type GameHistoryEntry, type TileType, TILE_DISPLAY_NAMES } from '@/types/mahjong'

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString()
}

function shantenLabel(n: number): string {
  if (n < 0) return 'Complete'
  if (n === 0) return 'Tenpai'
  return `${n}-shanten`
}

function shantenVariant(n: number): 'success' | 'warning' | 'secondary' {
  if (n <= 0) return 'success'
  if (n === 1) return 'warning'
  return 'secondary'
}

function parseTiles(raw: string): string {
  return raw
    .split(',')
    .map(t => TILE_DISPLAY_NAMES[t as TileType] ?? t)
    .join(' ')
}

interface GameHistoryProps {
  onLoadEntry?: (hand: TileType[], drawnTile: TileType | null, discardTiles: TileType[]) => void
}

export function GameHistory({ onLoadEntry }: GameHistoryProps) {
  const [entries, setEntries] = useState<GameHistoryEntry[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch('/api/history')
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data: GameHistoryEntry[] = await res.json()
      setEntries(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load history')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void load() }, [])

  function handleLoadEntry(entry: GameHistoryEntry) {
    if (!onLoadEntry) return
    
    const handTiles: TileType[] = entry.hand_tiles.split(',').filter(t => t) as TileType[]
    const drawnTile: TileType | null = entry.drawn_tile as TileType | null
    const discardTiles: TileType[] = entry.discard_tiles 
      ? entry.discard_tiles.split(',').filter(t => t) as TileType[]
      : []
    
    onLoadEntry(handTiles, drawnTile, discardTiles)
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-muted-foreground">
          {entries.length} request{entries.length !== 1 ? 's' : ''} recorded
        </h2>
        <Button variant="outline" size="sm" onClick={load} disabled={loading}>
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {error && (
        <Card className="border-destructive/50 bg-destructive/5">
          <CardContent className="py-3 text-sm text-destructive">{error}</CardContent>
        </Card>
      )}

      {!loading && !error && entries.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-muted-foreground text-sm">
            No history yet. Submit a hand to get started.
          </CardContent>
        </Card>
      )}

      <div className="space-y-2">
        {entries.map(entry => (
          <Card key={entry.id}>
            <CardHeader className="py-3 px-4">
              <CardTitle className="text-sm flex items-center gap-2">
                <Clock className="w-3 h-3 text-muted-foreground" />
                <span className="text-muted-foreground font-normal">{formatDate(entry.created_at)}</span>
                <Badge variant={shantenVariant(entry.current_shanten)} className="ml-auto">
                  {shantenLabel(entry.current_shanten)}
                </Badge>
                {onLoadEntry && (
                  <Button 
                    variant="outline" 
                    size="sm" 
                    onClick={() => handleLoadEntry(entry)}
                    className="h-6 px-2 text-xs border-primary/20 hover:border-primary/40 hover:bg-primary/5"
                  >
                    <Upload className="w-3 h-3 mr-1" />
                    Load
                  </Button>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent className="py-2 px-4 pt-0 space-y-1 text-xs">
              <div className="flex gap-2">
                <span className="text-muted-foreground w-20 flex-shrink-0">Hand</span>
                <span className="font-mono text-foreground">{parseTiles(entry.hand_tiles)}</span>
              </div>
              {entry.drawn_tile && (
                <div className="flex gap-2">
                  <span className="text-muted-foreground w-20 flex-shrink-0">Drawn</span>
                  <span className="font-mono text-foreground">
                    {TILE_DISPLAY_NAMES[entry.drawn_tile as TileType] ?? entry.drawn_tile}
                  </span>
                </div>
              )}
              {entry.discard_tiles && (
                <div className="flex gap-2">
                  <span className="text-muted-foreground w-20 flex-shrink-0">Discards</span>
                  <span className="font-mono text-foreground">{parseTiles(entry.discard_tiles)}</span>
                </div>
              )}
              {entry.best_discard && (
                <div className="flex gap-2">
                  <span className="text-muted-foreground w-20 flex-shrink-0">Best discard</span>
                  <span className="font-mono text-primary font-semibold">
                    {TILE_DISPLAY_NAMES[entry.best_discard as TileType] ?? entry.best_discard}
                  </span>
                  <span className="text-muted-foreground">
                    ({(entry.best_confidence * 100).toFixed(0)}% confidence)
                  </span>
                </div>
              )}
              <div className="flex gap-2">
                <span className="text-muted-foreground w-20 flex-shrink-0">Suggestions</span>
                <span>{entry.suggestion_count}</span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
