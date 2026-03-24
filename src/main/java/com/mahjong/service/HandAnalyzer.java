package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileSuit;
import com.mahjong.model.TileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HandAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(HandAnalyzer.class);

    public List<Tile> sortHand(List<Tile> hand) {
        List<Tile> sorted = new ArrayList<>(hand);
        sorted.sort(Comparator
            .comparing((Tile t) -> t.getType().getSuit())
            .thenComparing(t -> t.getType().getValue())
            .thenComparing(Tile::isRed));
        return sorted;
    }

    public Map<TileType, Integer> getTileCounts(List<Tile> tiles) {
        Map<TileType, Integer> counts = new HashMap<>();
        for (Tile tile : tiles) {
            counts.merge(tile.getType(), 1, Integer::sum);
        }
        return counts;
    }

    public Map<TileSuit, List<Tile>> groupBySuit(List<Tile> tiles) {
        return tiles.stream()
            .collect(Collectors.groupingBy(Tile::getSuit));
    }

    public boolean isPair(List<Tile> tiles) {
        if (tiles.size() != 2) return false;
        return tiles.get(0).getType() == tiles.get(1).getType();
    }

    public boolean isSequence(List<Tile> tiles) {
        if (tiles.size() != 3) return false;
        
        List<Tile> sorted = sortHand(tiles);
        TileSuit suit = sorted.get(0).getSuit();
        
        if (suit == TileSuit.HONOR) return false;
        
        for (Tile tile : sorted) {
            if (tile.getSuit() != suit) return false;
        }
        
        int val1 = sorted.get(0).getValue();
        int val2 = sorted.get(1).getValue();
        int val3 = sorted.get(2).getValue();
        
        return (val2 == val1 + 1) && (val3 == val2 + 1);
    }

    public boolean isTriplet(List<Tile> tiles) {
        if (tiles.size() != 3) return false;
        TileType type = tiles.get(0).getType();
        return tiles.stream().allMatch(t -> t.getType() == type);
    }

    public boolean isQuad(List<Tile> tiles) {
        if (tiles.size() != 4) return false;
        TileType type = tiles.get(0).getType();
        return tiles.stream().allMatch(t -> t.getType() == type);
    }

    public boolean hasValidStructure(List<Tile> hand) {
        int size = hand.size();
        return size == 14 || size == 13;
    }

    public List<TileType> getWaitingTiles(List<Tile> hand) {
        if (hand.size() != 13) {
            return Collections.emptyList();
        }
        
        List<TileType> waitingTiles = new ArrayList<>();
        
        for (TileType type : TileType.values()) {
            List<Tile> testHand = new ArrayList<>(hand);
            testHand.add(new Tile(type));
            
            if (isWinningHand(testHand)) {
                waitingTiles.add(type);
            }
        }
        
        return waitingTiles;
    }

    public boolean isWinningHand(List<Tile> hand) {
        if (hand.size() != 14) {
            return false;
        }
        
        if (isSevenPairs(hand)) {
            return true;
        }
        
        if (isThirteenOrphans(hand)) {
            return true;
        }
        
        return isStandardForm(hand);
    }

    private boolean isSevenPairs(List<Tile> hand) {
        if (hand.size() != 14) return false;
        
        Map<TileType, Integer> counts = getTileCounts(hand);
        
        if (counts.size() != 7) return false;
        
        return counts.values().stream().allMatch(count -> count == 2);
    }

    private boolean isThirteenOrphans(List<Tile> hand) {
        if (hand.size() != 14) return false;
        
        Set<TileType> terminals = Set.of(
            TileType.M1, TileType.M9,
            TileType.P1, TileType.P9,
            TileType.S1, TileType.S9,
            TileType.EAST, TileType.SOUTH, TileType.WEST, TileType.NORTH,
            TileType.WHITE, TileType.GREEN, TileType.RED
        );
        
        Map<TileType, Integer> counts = getTileCounts(hand);
        
        for (TileType terminal : terminals) {
            if (!counts.containsKey(terminal)) {
                return false;
            }
        }
        
        int pairCount = 0;
        for (Map.Entry<TileType, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == 2) {
                pairCount++;
            } else if (entry.getValue() != 1) {
                return false;
            }
        }
        
        return pairCount == 1;
    }

    private boolean isStandardForm(List<Tile> hand) {
        if (hand.size() != 14) return false;
        
        Map<TileType, Integer> counts = getTileCounts(hand);
        
        for (TileType pairType : counts.keySet()) {
            if (counts.get(pairType) >= 2) {
                Map<TileType, Integer> remaining = new HashMap<>(counts);
                remaining.merge(pairType, -2, Integer::sum);
                if (remaining.get(pairType) == 0) {
                    remaining.remove(pairType);
                }
                
                if (canFormMelds(remaining)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean canFormMelds(Map<TileType, Integer> tiles) {
        if (tiles.isEmpty()) {
            return true;
        }
        
        TileType firstType = tiles.keySet().iterator().next();
        int count = tiles.get(firstType);
        
        if (count >= 3) {
            Map<TileType, Integer> afterTriplet = new HashMap<>(tiles);
            afterTriplet.merge(firstType, -3, Integer::sum);
            if (afterTriplet.get(firstType) == 0) {
                afterTriplet.remove(firstType);
            }
            
            if (canFormMelds(afterTriplet)) {
                return true;
            }
        }
        
        if (firstType.getSuit() != TileSuit.HONOR && firstType.getValue() <= 7) {
            TileType next1 = getNextTileType(firstType);
            TileType next2 = getNextTileType(next1);
            
            if (next1 != null && next2 != null && 
                tiles.containsKey(next1) && tiles.containsKey(next2)) {
                
                Map<TileType, Integer> afterSequence = new HashMap<>(tiles);
                afterSequence.merge(firstType, -1, Integer::sum);
                afterSequence.merge(next1, -1, Integer::sum);
                afterSequence.merge(next2, -1, Integer::sum);
                
                if (afterSequence.get(firstType) == 0) afterSequence.remove(firstType);
                if (afterSequence.get(next1) == 0) afterSequence.remove(next1);
                if (afterSequence.get(next2) == 0) afterSequence.remove(next2);
                
                if (canFormMelds(afterSequence)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private TileType getNextTileType(TileType type) {
        TileSuit suit = type.getSuit();
        int value = type.getValue();
        
        if (suit == TileSuit.HONOR || value >= 9) {
            return null;
        }
        
        String prefix = switch (suit) {
            case MANZU -> "M";
            case PINZU -> "P";
            case SOUZU -> "S";
            default -> null;
        };
        
        if (prefix == null) return null;
        
        return TileType.valueOf(prefix + (value + 1));
    }

    public int countTilesInHand(List<Tile> hand, TileType type) {
        return (int) hand.stream()
            .filter(t -> t.getType() == type)
            .count();
    }
}
