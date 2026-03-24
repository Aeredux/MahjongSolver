package com.mahjong.service;

import com.mahjong.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MoveSuggestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoveSuggestionService.class);

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Autowired
    private HandAnalyzer handAnalyzer;

    public List<MoveSuggestion> suggestMoves(List<Tile> hand) {
        if (hand == null || hand.isEmpty()) {
            logger.warn("Cannot suggest moves for empty hand");
            return Collections.emptyList();
        }

        logger.debug("Generating move suggestions for hand with {} tiles", hand.size());

        int currentShanten = shantenCalculator.calculateShanten(hand);
        logger.debug("Current hand shanten: {}", currentShanten);

        Set<TileType> uniqueTiles = hand.stream()
            .map(Tile::getType)
            .collect(Collectors.toSet());

        List<MoveSuggestion> suggestions = new ArrayList<>();

        for (TileType tileType : uniqueTiles) {
            int shantenAfterDiscard = shantenCalculator.calculateShantenAfterDiscard(hand, tileType);
            
            MoveSuggestion suggestion = new MoveSuggestion(tileType, shantenAfterDiscard);
            
            int ukeire = calculateUkeire(hand, tileType);
            suggestion.setUkeireCount(ukeire);
            
            double confidence = calculateConfidence(currentShanten, shantenAfterDiscard, ukeire);
            suggestion.setConfidence(confidence);
            
            String reasoning = generateReasoning(currentShanten, shantenAfterDiscard, ukeire, tileType);
            suggestion.setReasoning(reasoning);
            
            suggestions.add(suggestion);
        }

        suggestions.sort(Comparator
            .comparing(MoveSuggestion::getShantenAfterDiscard)
            .thenComparing(MoveSuggestion::getUkeireCount, Comparator.reverseOrder())
            .thenComparing(MoveSuggestion::getConfidence, Comparator.reverseOrder()));

        logger.debug("Generated {} move suggestions", suggestions.size());
        
        return suggestions;
    }

    private int calculateUkeire(List<Tile> hand, TileType discardType) {
        List<Tile> handAfterDiscard = new ArrayList<>(hand);
        
        for (int i = 0; i < handAfterDiscard.size(); i++) {
            if (handAfterDiscard.get(i).getType() == discardType) {
                handAfterDiscard.remove(i);
                break;
            }
        }

        if (handAfterDiscard.size() != 13) {
            return 0;
        }

        Map<TileType, Integer> currentCounts = getTileCounts(handAfterDiscard);
        
        int ukeire = 0;
        int targetShanten = shantenCalculator.calculateShanten(handAfterDiscard);

        for (TileType type : TileType.values()) {
            int tilesInHand = currentCounts.getOrDefault(type, 0);
            int tilesRemaining = 4 - tilesInHand;
            
            if (tilesRemaining > 0) {
                List<Tile> testHand = new ArrayList<>(handAfterDiscard);
                testHand.add(new Tile(type));
                
                int shantenAfterDraw = shantenCalculator.calculateShanten(testHand);
                
                if (shantenAfterDraw < targetShanten) {
                    ukeire += tilesRemaining;
                }
            }
        }

        return ukeire;
    }

    private double calculateConfidence(int currentShanten, int shantenAfterDiscard, int ukeire) {
        if (shantenAfterDiscard < currentShanten) {
            return 1.0;
        } else if (shantenAfterDiscard == currentShanten) {
            return 0.5 + (ukeire / 136.0);
        } else {
            return 0.0;
        }
    }

    private String generateReasoning(int currentShanten, int shantenAfterDiscard, int ukeire, TileType tileType) {
        StringBuilder reasoning = new StringBuilder();

        if (shantenAfterDiscard < currentShanten) {
            reasoning.append("Improves shanten from ")
                    .append(currentShanten)
                    .append(" to ")
                    .append(shantenAfterDiscard)
                    .append(". ");
        } else if (shantenAfterDiscard == currentShanten) {
            reasoning.append("Maintains shanten at ")
                    .append(currentShanten)
                    .append(". ");
        } else {
            reasoning.append("Worsens shanten from ")
                    .append(currentShanten)
                    .append(" to ")
                    .append(shantenAfterDiscard)
                    .append(". ");
        }

        if (ukeire > 0) {
            reasoning.append("Ukeire: ")
                    .append(ukeire)
                    .append(" tiles (")
                    .append(String.format("%.1f", (ukeire / 136.0) * 100))
                    .append("% of remaining tiles). ");
        }

        if (tileType.isTerminalOrHonor()) {
            reasoning.append("Terminal/honor tile - may be safer to discard. ");
        }

        return reasoning.toString().trim();
    }

    public MoveSuggestion getBestMove(List<Tile> hand) {
        List<MoveSuggestion> suggestions = suggestMoves(hand);
        
        if (suggestions.isEmpty()) {
            return null;
        }

        return suggestions.get(0);
    }

    public List<MoveSuggestion> getTopMoves(List<Tile> hand, int count) {
        List<MoveSuggestion> suggestions = suggestMoves(hand);
        
        return suggestions.stream()
            .limit(count)
            .collect(Collectors.toList());
    }

    private Map<TileType, Integer> getTileCounts(List<Tile> tiles) {
        Map<TileType, Integer> counts = new HashMap<>();
        for (Tile tile : tiles) {
            counts.merge(tile.getType(), 1, Integer::sum);
        }
        return counts;
    }

    public String explainMove(MoveSuggestion suggestion) {
        if (suggestion == null) {
            return "No move suggestion available.";
        }

        return String.format(
            "Discard %s: Shanten = %d, Confidence = %.2f, %s",
            suggestion.getDiscardTile(),
            suggestion.getShantenAfterDiscard(),
            suggestion.getConfidence(),
            suggestion.getReasoning()
        );
    }
}
