package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileSuit;
import com.mahjong.model.TileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShantenCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(ShantenCalculator.class);
    private static final int MAX_SHANTEN = 8;

    public int calculateShanten(List<Tile> hand) {
        if (hand.isEmpty()) {
            return MAX_SHANTEN;
        }

        int standardShanten = calculateStandardShanten(hand);
        int chiitoitsuShanten = calculateChiitoitsuShanten(hand);
        int kokushiShanten = calculateKokushiShanten(hand);

        int minShanten = Math.min(standardShanten, Math.min(chiitoitsuShanten, kokushiShanten));
        
        logger.debug("Shanten calculation - Standard: {}, Chiitoitsu: {}, Kokushi: {}, Min: {}", 
                    standardShanten, chiitoitsuShanten, kokushiShanten, minShanten);
        
        return minShanten;
    }

    private int calculateStandardShanten(List<Tile> hand) {
        Map<TileType, Integer> counts = getTileCounts(hand);
        
        int minShanten = MAX_SHANTEN;
        
        // Try with each possible pair
        for (TileType pairType : TileType.values()) {
            if (counts.getOrDefault(pairType, 0) >= 2) {
                Map<TileType, Integer> remaining = new HashMap<>(counts);
                remaining.merge(pairType, -2, Integer::sum);
                if (remaining.get(pairType) == 0) {
                    remaining.remove(pairType);
                }
                
                int shanten = calculateShantenWithPair(remaining, 0, 0);
                minShanten = Math.min(minShanten, shanten);
            }
        }
        
        // Try without a pair
        int shantenNoPair = calculateShantenWithoutPair(counts, 0, 0);
        minShanten = Math.min(minShanten, shantenNoPair);
        
        return minShanten;
    }
    
    private int calculateShantenWithPair(Map<TileType, Integer> tiles, int melds, int tatsu) {
        if (tiles.isEmpty()) {
            return 8 - melds * 2 - tatsu - 1;
        }
        
        if (melds + tatsu >= 4) {
            return 8 - melds * 2 - tatsu - 1;
        }
        
        return calculateMeldFormation(tiles, melds, tatsu, true);
    }

    private int calculateShantenWithoutPair(Map<TileType, Integer> tiles, int melds, int tatsu) {
        if (tiles.isEmpty()) {
            return 8 - melds * 2 - tatsu;
        }
        
        if (melds + tatsu >= 5) {
            return 8 - melds * 2 - tatsu;
        }
        
        return calculateMeldFormation(tiles, melds, tatsu, false);
    }
    
    private int calculateMeldFormation(Map<TileType, Integer> tiles, int melds, int tatsu, boolean hasPair) {
        if (tiles.isEmpty()) {
            return hasPair ? (8 - melds * 2 - tatsu - 1) : (8 - melds * 2 - tatsu);
        }
        
        int maxGroups = hasPair ? 4 : 5;
        if (melds + tatsu >= maxGroups) {
            return hasPair ? (8 - melds * 2 - tatsu - 1) : (8 - melds * 2 - tatsu);
        }
        
        TileType firstType = tiles.keySet().stream()
            .min(Comparator.comparing(TileType::getSuit).thenComparing(TileType::getValue))
            .orElse(null);
        
        if (firstType == null) {
            return hasPair ? (8 - melds * 2 - tatsu - 1) : (8 - melds * 2 - tatsu);
        }
        
        int count = tiles.get(firstType);
        int minShanten = MAX_SHANTEN;
        
        // Try forming a triplet
        if (count >= 3) {
            Map<TileType, Integer> afterTriplet = new HashMap<>(tiles);
            afterTriplet.merge(firstType, -3, Integer::sum);
            if (afterTriplet.get(firstType) == 0) {
                afterTriplet.remove(firstType);
            }
            minShanten = Math.min(minShanten, calculateMeldFormation(afterTriplet, melds + 1, tatsu, hasPair));
        }
        
        // Try forming a pair (tatsu)
        if (count >= 2) {
            Map<TileType, Integer> afterPair = new HashMap<>(tiles);
            afterPair.merge(firstType, -2, Integer::sum);
            if (afterPair.get(firstType) == 0) {
                afterPair.remove(firstType);
            }
            minShanten = Math.min(minShanten, calculateMeldFormation(afterPair, melds, tatsu + 1, hasPair));
        }
        
        // Try forming sequences (only for suited tiles)
        if (firstType.getSuit() != TileSuit.HONOR && firstType.getValue() <= 7) {
            TileType next1 = getNextTileType(firstType);
            TileType next2 = getNextTileType(next1);
            
            // Complete sequence
            if (next1 != null && next2 != null && tiles.containsKey(next1) && tiles.containsKey(next2)) {
                Map<TileType, Integer> afterSequence = new HashMap<>(tiles);
                afterSequence.merge(firstType, -1, Integer::sum);
                afterSequence.merge(next1, -1, Integer::sum);
                afterSequence.merge(next2, -1, Integer::sum);
                
                if (afterSequence.get(firstType) == 0) afterSequence.remove(firstType);
                if (afterSequence.get(next1) == 0) afterSequence.remove(next1);
                if (afterSequence.get(next2) == 0) afterSequence.remove(next2);
                
                minShanten = Math.min(minShanten, calculateMeldFormation(afterSequence, melds + 1, tatsu, hasPair));
            }
            
            // Ryanmen (two consecutive tiles)
            if (next1 != null && tiles.containsKey(next1)) {
                Map<TileType, Integer> afterRyanmen = new HashMap<>(tiles);
                afterRyanmen.merge(firstType, -1, Integer::sum);
                afterRyanmen.merge(next1, -1, Integer::sum);
                
                if (afterRyanmen.get(firstType) == 0) afterRyanmen.remove(firstType);
                if (afterRyanmen.get(next1) == 0) afterRyanmen.remove(next1);
                
                minShanten = Math.min(minShanten, calculateMeldFormation(afterRyanmen, melds, tatsu + 1, hasPair));
            }
            
            // Kanchan (tiles with one gap)
            if (next2 != null && tiles.containsKey(next2)) {
                Map<TileType, Integer> afterKanchan = new HashMap<>(tiles);
                afterKanchan.merge(firstType, -1, Integer::sum);
                afterKanchan.merge(next2, -1, Integer::sum);
                
                if (afterKanchan.get(firstType) == 0) afterKanchan.remove(firstType);
                if (afterKanchan.get(next2) == 0) afterKanchan.remove(next2);
                
                minShanten = Math.min(minShanten, calculateMeldFormation(afterKanchan, melds, tatsu + 1, hasPair));
            }
        }
        
        // Discard this tile
        Map<TileType, Integer> afterDiscard = new HashMap<>(tiles);
        afterDiscard.merge(firstType, -1, Integer::sum);
        if (afterDiscard.get(firstType) == 0) {
            afterDiscard.remove(firstType);
        }
        minShanten = Math.min(minShanten, calculateMeldFormation(afterDiscard, melds, tatsu, hasPair));
        
        return minShanten;
    }

    private int calculateChiitoitsuShanten(List<Tile> hand) {
        Map<TileType, Integer> counts = getTileCounts(hand);
        
        int pairs = 0;
        int singles = 0;
        
        for (int count : counts.values()) {
            if (count >= 2) {
                pairs++;
            } else if (count == 1) {
                singles++;
            }
        }
        
        return 6 - pairs;
    }

    private int calculateKokushiShanten(List<Tile> hand) {
        Set<TileType> terminals = Set.of(
            TileType.M1, TileType.M9,
            TileType.P1, TileType.P9,
            TileType.S1, TileType.S9,
            TileType.EAST, TileType.SOUTH, TileType.WEST, TileType.NORTH,
            TileType.WHITE, TileType.GREEN, TileType.RED
        );
        
        Map<TileType, Integer> counts = getTileCounts(hand);
        
        int uniqueTerminals = 0;
        boolean hasPair = false;
        
        for (TileType terminal : terminals) {
            int count = counts.getOrDefault(terminal, 0);
            if (count > 0) {
                uniqueTerminals++;
                if (count >= 2) {
                    hasPair = true;
                }
            }
        }
        
        int shanten = 13 - uniqueTerminals - (hasPair ? 1 : 0);
        
        return shanten;
    }

    private Map<TileType, Integer> getTileCounts(List<Tile> tiles) {
        Map<TileType, Integer> counts = new HashMap<>();
        for (Tile tile : tiles) {
            counts.merge(tile.getType(), 1, Integer::sum);
        }
        return counts;
    }

    private TileType getNextTileType(TileType type) {
        if (type == null) return null;
        
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

    public int calculateShantenAfterDiscard(List<Tile> hand, TileType discardType) {
        List<Tile> newHand = new ArrayList<>(hand);
        
        boolean removed = false;
        for (int i = 0; i < newHand.size(); i++) {
            if (newHand.get(i).getType() == discardType) {
                newHand.remove(i);
                removed = true;
                break;
            }
        }
        
        if (!removed) {
            logger.warn("Attempted to discard tile {} not in hand", discardType);
            return MAX_SHANTEN;
        }
        
        return calculateShanten(newHand);
    }

    public boolean isTenpai(List<Tile> hand) {
        return calculateShanten(hand) == 0;
    }
}
