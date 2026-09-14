package com.mahjong.service;

import com.mahjong.dto.MeldDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.MoveSuggestion;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MoveSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(MoveSuggestionService.class);

    @Autowired
    private ShantenCalculator shantenCalculator;

    public List<MoveSuggestion> suggestMoves(List<Tile> hand) {
        return suggestMoves(hand, Collections.emptyList(), Collections.emptyList(), null, null,
                Collections.emptyList(), Collections.emptyList());
    }

    public List<MoveSuggestion> suggestMoves(List<Tile> hand, List<PlayerDiscardsDTO> opponents) {
        return suggestMoves(hand, opponents, Collections.emptyList(), null, null,
                Collections.emptyList(), Collections.emptyList());
    }

    public List<MoveSuggestion> suggestMoves(List<Tile> hand, List<PlayerDiscardsDTO> opponents, List<TileType> ownDiscards) {
        return suggestMoves(hand, opponents, ownDiscards, null, null,
                Collections.emptyList(), Collections.emptyList());
    }

    public List<MoveSuggestion> suggestMoves(
            List<Tile> hand,
            List<PlayerDiscardsDTO> opponents,
            List<TileType> ownDiscards,
            Wind seatWind,
            Wind roundWind
    ) {
        return suggestMoves(hand, opponents, ownDiscards, seatWind, roundWind,
                Collections.emptyList(), Collections.emptyList());
    }

    public List<MoveSuggestion> suggestMoves(
            List<Tile> hand,
            List<PlayerDiscardsDTO> opponents,
            List<TileType> ownDiscards,
            Wind seatWind,
            Wind roundWind,
            List<MeldDTO> ownMelds,
            List<TileType> dora
    ) {
        if (hand == null || hand.isEmpty()) {
            logger.warn("Cannot suggest moves for empty hand");
            return Collections.emptyList();
        }

        logger.debug("Generating move suggestions for hand with {} tiles", hand.size());

        List<MeldDTO> safeMelds = ownMelds != null ? ownMelds : Collections.emptyList();
        List<TileType> safeDora = dora != null ? dora : Collections.emptyList();
        Map<TileType, DiscardAnalysis> analyses = shantenCalculator.analyzeDiscards(hand, safeMelds);
        int currentShanten = analyses.values().stream()
                .mapToInt(DiscardAnalysis::getShanten)
                .min()
                .orElseGet(() -> shantenCalculator.calculateShanten(hand, safeMelds));

        List<PlayerDiscardsDTO> safeOpponents = opponents != null ? opponents : Collections.emptyList();
        List<TileType> safeOwnDiscards = ownDiscards != null ? ownDiscards : Collections.emptyList();
        DefenseHeuristics.DefenseContext defense = DefenseHeuristics.build(
                hand, safeOpponents, safeOwnDiscards, safeMelds, safeDora);

        List<RankedSuggestion> ranked = new ArrayList<>();
        for (DiscardAnalysis analysis : analyses.values()) {
            int ukeire = DefenseHeuristics.ukeireCount(analysis.getAdvance(), defense.wallRemaining());
            int goodShape = DefenseHeuristics.ukeireCount(analysis.getGoodShapeAdvance(), defense.wallRemaining());
            if (analysis.getShanten() == 0 && goodShape == 0) {
                goodShape = tenpaiWaitQuality(analysis.getAdvance());
            }
            DefenseHeuristics.TileDefense tileDefense = DefenseHeuristics.evaluate(analysis.getDiscard(), defense);
            int yakuhaiKeep = yakuhaiKeepValue(analysis.getDiscard(), seatWind, roundWind);
            int doraKeep = doraKeepValue(analysis.getDiscard(), safeDora);
            int keepValue = yakuhaiKeep + doraKeep;

            MoveSuggestion suggestion = new MoveSuggestion(analysis.getDiscard(), analysis.getShanten());
            suggestion.setUkeireCount(ukeire);
            suggestion.setConfidence(calculateConfidence(currentShanten, analysis.getShanten(), ukeire));
            suggestion.setReasoning(generateReasoning(
                    currentShanten, analysis, ukeire, goodShape, tileDefense, keepValue, defense, safeDora));
            ranked.add(new RankedSuggestion(
                    suggestion, goodShape, tileDefense.dangerScore(), doraKeep, yakuhaiKeep));
        }

        ranked.sort(Comparator
                .comparingInt((RankedSuggestion r) -> r.suggestion.getShantenAfterDiscard())
                .thenComparingInt(r -> r.doraKeep)
                .thenComparing(Comparator.comparingInt((RankedSuggestion r) -> r.suggestion.getUkeireCount()).reversed())
                .thenComparing(Comparator.comparingInt((RankedSuggestion r) -> r.goodShape).reversed())
                .thenComparingInt(r -> r.dangerScore)
                .thenComparingInt(r -> r.yakuhaiKeep));

        List<MoveSuggestion> suggestions = ranked.stream()
                .map(r -> r.suggestion)
                .collect(Collectors.toList());

        logger.debug("Generated {} move suggestions", suggestions.size());
        return suggestions;
    }

    /**
     * Ryanmen-style waits (two consecutive suited numbers) score higher than tanki / kanchan.
     */
    private static int tenpaiWaitQuality(java.util.Set<TileType> waits) {
        if (waits.size() >= 2) {
            return waits.size() * 4;
        }
        return waits.size();
    }

    /**
     * Higher keep-value sorts later (worse discard) when shanten / ukeire / shape / defense tie.
     */
    static int yakuhaiKeepValue(TileType tile, Wind seatWind, Wind roundWind) {
        if (tile.isDragon()) {
            return 2;
        }
        if (seatWind != null && tile == seatWind.toTileType()) {
            return 2;
        }
        if (roundWind != null && tile == roundWind.toTileType()) {
            return 2;
        }
        return 0;
    }

    /**
     * Doman: the panel tile IS the dora (no Tenhou indicator +1). Sorts after
     * shanten and before ukeire so a 1-copy panel does not make discarding dora "more efficient".
     */
    static int doraKeepValue(TileType tile, List<TileType> dora) {
        if (tile == null || dora == null || dora.isEmpty()) {
            return 0;
        }
        for (TileType indicator : dora) {
            if (indicator == tile) {
                return 3;
            }
        }
        return 0;
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

    private String generateReasoning(
            int currentShanten,
            DiscardAnalysis analysis,
            int ukeire,
            int goodShape,
            DefenseHeuristics.TileDefense tileDefense,
            int keepValue,
            DefenseHeuristics.DefenseContext defense,
            List<TileType> dora
    ) {
        StringBuilder reasoning = new StringBuilder();
        int shantenAfterDiscard = analysis.getShanten();
        TileType tileType = analysis.getDiscard();

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

        int wallSize = Math.max(1, defense.wallRemaining().values().stream().mapToInt(Integer::intValue).sum());
        if (ukeire > 0) {
            reasoning.append("Ukeire: ")
                    .append(ukeire)
                    .append(" tiles (")
                    .append(String.format("%.1f", (ukeire / (double) wallSize) * 100))
                    .append("% of remaining wall). ");
            if (goodShape > 0 && shantenAfterDiscard == 1) {
                reasoning.append("Good-shape ukeire: ").append(goodShape).append(". ");
            }
            int copiesLeft = defense.wallRemaining().getOrDefault(tileType, 0);
            if (copiesLeft <= 1) {
                reasoning.append("Only ").append(copiesLeft).append(" cop").append(copiesLeft == 1 ? "y" : "ies")
                        .append(" of ").append(tileType).append(" left in wall. ");
            }
        }

        for (String note : tileDefense.notes()) {
            reasoning.append(note).append(' ');
        }

        if (keepValue > 0) {
            if (doraKeepValue(tileType, dora) > 0) {
                reasoning.append("Dora — prefer to keep if other discards are equal. ");
            }
            if (keepValue > doraKeepValue(tileType, dora)) {
                reasoning.append("Yakuhai — prefer to keep if other discards are equal. ");
            }
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

    private record RankedSuggestion(
            MoveSuggestion suggestion,
            int goodShape,
            int dangerScore,
            int doraKeep,
            int yakuhaiKeep
    ) {
    }
}
