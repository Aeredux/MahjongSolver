package com.mahjong.service;

import com.mahjong.dto.DiscardedTileDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
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
    
    private static final int MAX_SHANTEN = 8;

    public List<MoveSuggestion> suggestMoves(List<Tile> hand) {
        return suggestMoves(hand, Collections.emptyList(), Collections.emptyList());
    }

    public List<MoveSuggestion> suggestMoves(List<Tile> hand, List<PlayerDiscardsDTO> opponents) {
        return suggestMoves(hand, opponents, Collections.emptyList());
    }

    public List<MoveSuggestion> suggestMoves(List<Tile> hand, List<PlayerDiscardsDTO> opponents, List<TileType> ownDiscards) {
        if (hand == null || hand.isEmpty()) {
            logger.warn("Cannot suggest moves for empty hand");
            return Collections.emptyList();
        }

        logger.debug("Generating move suggestions for hand with {} tiles", hand.size());

        // For 14-tile hands, calculate shanten based on best possible discard
        // For 13-tile hands, use current shanten
        int currentShanten;
        if (hand.size() == 14) {
            // Find minimum shanten after any discard
            currentShanten = hand.stream()
                .map(Tile::getType)
                .distinct()
                .mapToInt(type -> shantenCalculator.calculateShantenAfterDiscard(hand, type))
                .min()
                .orElse(MAX_SHANTEN);
        } else {
            currentShanten = shantenCalculator.calculateShanten(hand);
        }
        logger.debug("Current hand shanten: {}", currentShanten);

        Set<TileType> uniqueTiles = hand.stream()
            .map(Tile::getType)
            .collect(Collectors.toSet());

        List<PlayerDiscardsDTO> safeOpponents = opponents != null ? opponents : Collections.emptyList();
        List<TileType> safeOwnDiscards = ownDiscards != null ? ownDiscards : Collections.emptyList();
        Map<TileType, Integer> visibleCounts = buildVisibleCounts(safeOpponents, safeOwnDiscards);
        Map<Wind, Set<TileType>> genbutsuBySeat = buildGenbutsuMap(safeOpponents);
        Map<Wind, Integer> tenpaiDanger = buildTenpaiDanger(safeOpponents);

        List<MoveSuggestion> suggestions = new ArrayList<>();

        for (TileType tileType : uniqueTiles) {
            int shantenAfterDiscard = shantenCalculator.calculateShantenAfterDiscard(hand, tileType);

            MoveSuggestion suggestion = new MoveSuggestion(tileType, shantenAfterDiscard);

            int ukeire = calculateUkeire(hand, tileType, visibleCounts);
            suggestion.setUkeireCount(ukeire);

            double confidence = calculateConfidence(currentShanten, shantenAfterDiscard, ukeire);
            suggestion.setConfidence(confidence);

            String reasoning = generateReasoning(currentShanten, shantenAfterDiscard, ukeire, tileType,
                    visibleCounts, genbutsuBySeat, tenpaiDanger);
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

    private int calculateUkeire(List<Tile> hand, TileType discardType, Map<TileType, Integer> visibleCounts) {
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
            int tilesDiscarded = visibleCounts.getOrDefault(type, 0);
            int tilesRemaining = Math.max(0, 4 - tilesInHand - tilesDiscarded);

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

    private Map<TileType, Integer> buildVisibleCounts(List<PlayerDiscardsDTO> opponents, List<TileType> ownDiscards) {
        Map<TileType, Integer> counts = new HashMap<>();
        for (PlayerDiscardsDTO opponent : opponents) {
            if (opponent.getDiscards() != null) {
                for (DiscardedTileDTO d : opponent.getDiscards()) {
                    if (d.getTile() != null) {
                        counts.merge(d.getTile(), 1, Integer::sum);
                    }
                }
            }
        }
        for (TileType t : ownDiscards) {
            if (t != null) {
                counts.merge(t, 1, Integer::sum);
            }
        }
        return counts;
    }

    private Map<Wind, Set<TileType>> buildGenbutsuMap(List<PlayerDiscardsDTO> opponents) {
        Map<Wind, Set<TileType>> result = new HashMap<>();
        for (PlayerDiscardsDTO opponent : opponents) {
            if (opponent.isRiichi() && opponent.getDiscards() != null) {
                Set<TileType> safe = new HashSet<>();
                for (DiscardedTileDTO d : opponent.getDiscards()) {
                    if (d.getTile() != null) {
                        safe.add(d.getTile());
                    }
                }
                result.put(opponent.getWind(), safe);
            }
        }
        return result;
    }

    private Map<Wind, Integer> buildTenpaiDanger(List<PlayerDiscardsDTO> opponents) {
        Map<Wind, Integer> result = new HashMap<>();
        for (PlayerDiscardsDTO opponent : opponents) {
            if (opponent.getDiscards() == null || opponent.getDiscards().isEmpty()) continue;
            int consecutive = 0;
            List<DiscardedTileDTO> discards = opponent.getDiscards();
            for (int i = discards.size() - 1; i >= 0; i--) {
                if (discards.get(i).isTsumogiri()) {
                    consecutive++;
                } else {
                    break;
                }
            }
            result.put(opponent.getWind(), consecutive);
        }
        return result;
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

    private String generateReasoning(int currentShanten, int shantenAfterDiscard, int ukeire, TileType tileType,
            Map<TileType, Integer> visibleCounts, Map<Wind, Set<TileType>> genbutsuBySeat,
            Map<Wind, Integer> tenpaiDanger) {
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

        int totalVisible = visibleCounts.values().stream().mapToInt(Integer::intValue).sum();
        int wallSize = Math.max(1, 136 - totalVisible);
        if (ukeire > 0) {
            int copiesLeft = 4 - visibleCounts.getOrDefault(tileType, 0);
            reasoning.append("Ukeire: ")
                    .append(ukeire)
                    .append(" tiles (")
                    .append(String.format("%.1f", (ukeire / (double) wallSize) * 100))
                    .append("% of remaining wall). ");
            if (copiesLeft <= 1) {
                reasoning.append("Only ").append(copiesLeft).append(" cop").append(copiesLeft == 1 ? "y" : "ies")
                        .append(" of ").append(tileType).append(" left in wall. ");
            }
        }

        List<String> genbutsuAgainst = new ArrayList<>();
        for (Map.Entry<Wind, Set<TileType>> entry : genbutsuBySeat.entrySet()) {
            if (entry.getValue().contains(tileType)) {
                genbutsuAgainst.add(entry.getKey().name());
            }
        }
        if (!genbutsuAgainst.isEmpty()) {
            reasoning.append("Genbutsu safe vs ").append(String.join(", ", genbutsuAgainst)).append(" (riichi). ");
        }

        List<String> dangerousOpponents = new ArrayList<>();
        for (Map.Entry<Wind, Integer> entry : tenpaiDanger.entrySet()) {
            int consecutive = entry.getValue();
            if (consecutive >= 4) {
                dangerousOpponents.add(entry.getKey().name() + " (high tenpai danger: " + consecutive + " tsumogiri)");
            } else if (consecutive >= 2) {
                dangerousOpponents.add(entry.getKey().name() + " (possible tenpai: " + consecutive + " tsumogiri)");
            }
        }
        if (!dangerousOpponents.isEmpty()) {
            reasoning.append("Caution: ").append(String.join(", ", dangerousOpponents)).append(". ");
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
