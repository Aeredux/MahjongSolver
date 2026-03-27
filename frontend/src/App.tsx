import { useState } from 'react'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@radix-ui/react-tabs'
import { Sparkles, History } from 'lucide-react'
import { HandInput } from '@/components/HandInput'
import { MoveSuggestions } from '@/components/MoveSuggestions'
import { GameHistory } from '@/components/GameHistory'
import { type TileType, type MoveSuggestionResponse } from '@/types/mahjong'
import { cn } from '@/lib/utils'

function App() {
  const [result, setResult] = useState<MoveSuggestionResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState('suggest')
  const [loadedHand, setLoadedHand] = useState<{ hand: TileType[], drawnTile: TileType | null, discardTiles: TileType[] } | null>(null)
  const [discardTiles, setDiscardTiles] = useState<TileType[]>([])

  function handleDiscardTilesChange(discardTiles: TileType[]) {
    setDiscardTiles(discardTiles)
  }

  function handleLoadHand(hand: TileType[], drawnTile: TileType | null, discardTiles: TileType[] = []) {
    setLoadedHand({ hand, drawnTile, discardTiles })
    setDiscardTiles(discardTiles)
    setActiveTab('suggest')
    // Clear the loaded hand after a short delay to allow HandInput to process it
    setTimeout(() => setLoadedHand(null), 100)
  }

  async function handleSubmit(hand: TileType[], drawnTile: TileType | null) {
    setIsLoading(true)
    setError(null)
    setResult(null)
    try {
      const res = await fetch('/api/suggest-move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ hand, drawn_tile: drawnTile, discard_tiles: discardTiles }),
      })
      if (!res.ok) throw new Error(`Server returned ${res.status}`)
      const data: MoveSuggestionResponse = await res.json()
      setResult(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Request failed')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center gap-3">
          <span className="text-2xl select-none">🀄</span>
          <div>
            <h1 className="text-base font-bold text-foreground leading-none">Mahjong AI</h1>
            <p className="text-xs text-muted-foreground">Riichi move suggestions</p>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-6">
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="flex gap-1 mb-6 bg-secondary rounded-lg p-1 w-fit">
            {[
              { value: 'suggest', label: 'Suggest Move', icon: Sparkles },
              { value: 'history', label: 'History', icon: History },
            ].map(({ value, label, icon: Icon }) => (
              <TabsTrigger
                key={value}
                value={value}
                className={cn(
                  'flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-colors',
                  'text-muted-foreground hover:text-foreground',
                  'data-[state=active]:bg-background data-[state=active]:text-foreground data-[state=active]:shadow-sm'
                )}
              >
                <Icon className="w-4 h-4" />
                {label}
              </TabsTrigger>
            ))}
          </TabsList>

          <TabsContent value="suggest">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div>
                <HandInput onSubmit={handleSubmit} onDiscardTilesChange={handleDiscardTilesChange} isLoading={isLoading} loadedHand={loadedHand} />
              </div>
              <div>
                {error && (
                  <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-sm text-destructive mb-4">
                    {error}
                  </div>
                )}
                {isLoading && (
                  <div className="flex items-center justify-center py-16 text-muted-foreground">
                    <div className="flex flex-col items-center gap-2">
                      <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                      <span className="text-sm">Calculating…</span>
                    </div>
                  </div>
                )}
                {result && !isLoading && (
                  <MoveSuggestions response={result} />
                )}
                {!result && !isLoading && !error && (
                  <div className="flex flex-col items-center justify-center py-16 text-muted-foreground gap-3">
                    <span className="text-4xl select-none">🀄</span>
                    <p className="text-sm">Build your hand and click <strong>Get Suggestions</strong></p>
                  </div>
                )}
              </div>
            </div>
          </TabsContent>

          <TabsContent value="history">
            <GameHistory onLoadEntry={handleLoadHand} />
          </TabsContent>
        </Tabs>
      </main>
    </div>
  )
}

export default App
