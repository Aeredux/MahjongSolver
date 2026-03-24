import { Trophy, TrendingDown, Info } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Tile } from '@/components/Tile'
import { type MoveSuggestionResponse, type TileType, TILE_DISPLAY_NAMES } from '@/types/mahjong'
import { cn } from '@/lib/utils'

interface MoveSuggestionsProps {
  response: MoveSuggestionResponse
}

function shantenLabel(n: number): string {
  if (n < 0) return 'Complete hand'
  if (n === 0) return 'Tenpai'
  return `${n}-shanten`
}

function shantenBadgeVariant(n: number): 'success' | 'warning' | 'secondary' {
  if (n <= 0) return 'success'
  if (n === 1) return 'warning'
  return 'secondary'
}

function confidenceColor(c: number): string {
  if (c >= 0.9) return 'bg-green-500'
  if (c >= 0.7) return 'bg-amber-400'
  if (c >= 0.5) return 'bg-orange-400'
  return 'bg-slate-500'
}

export function MoveSuggestions({ response }: MoveSuggestionsProps) {
  const { suggestions, currentShanten } = response

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <span className="text-sm text-muted-foreground">Current hand:</span>
        <Badge variant={shantenBadgeVariant(currentShanten)}>
          {shantenLabel(currentShanten)}
        </Badge>
      </div>

      {suggestions.length === 0 ? (
        <Card>
          <CardContent className="py-6 text-center text-muted-foreground text-sm">
            No suggestions available for this hand.
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2">
          {suggestions.map((s, i) => (
            <Card
              key={`${s.discardTile}-${i}`}
              className={cn(
                'transition-colors',
                i === 0 && 'border-primary/50 bg-primary/5'
              )}
            >
              <CardHeader className="py-3 px-4">
                <CardTitle className="text-sm flex items-center gap-3">
                  {i === 0 && <Trophy className="w-4 h-4 text-primary flex-shrink-0" />}
                  <span className="text-muted-foreground font-normal w-5 text-right">{i + 1}.</span>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-muted-foreground">Discard</span>
                    <Tile tile={s.discardTile as TileType} size="sm" highlighted={i === 0} />
                    <span className="font-semibold">{TILE_DISPLAY_NAMES[s.discardTile as TileType]}</span>
                  </div>
                  <div className="ml-auto flex items-center gap-2 flex-shrink-0">
                    <Badge variant={shantenBadgeVariant(s.shantenAfterDiscard)}>
                      {shantenLabel(s.shantenAfterDiscard)}
                    </Badge>
                  </div>
                </CardTitle>
              </CardHeader>
              <CardContent className="py-2 px-4 pt-0 space-y-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted-foreground w-20 flex-shrink-0">Confidence</span>
                  <div className="flex-1 h-1.5 bg-secondary rounded-full overflow-hidden">
                    <div
                      className={cn('h-full rounded-full transition-all', confidenceColor(s.confidence))}
                      style={{ width: `${(s.confidence * 100).toFixed(0)}%` }}
                    />
                  </div>
                  <span className="text-xs font-mono w-10 text-right">
                    {(s.confidence * 100).toFixed(0)}%
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted-foreground w-20 flex-shrink-0">Ukeire</span>
                  <span className="text-xs font-semibold text-foreground flex items-center gap-1">
                    <TrendingDown className="w-3 h-3 text-muted-foreground" />
                    {s.ukeireCount} useful tiles
                  </span>
                </div>

                <div className="flex items-start gap-2">
                  <Info className="w-3 h-3 text-muted-foreground flex-shrink-0 mt-0.5" />
                  <p className="text-xs text-muted-foreground leading-relaxed">{s.reasoning}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
