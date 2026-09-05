package com.mahjong.service;

import com.mahjong.dto.MeldDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.MeldType;
import com.mahjong.model.MoveSuggestion;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MoveSuggestionServiceTest {

    @Autowired
    private MoveSuggestionService suggestionService;

    @Test
    void testSuggestMovesForTenpaiHand() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(tenpaiHand);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        MoveSuggestion bestMove = suggestions.get(0);
        assertEquals(0, bestMove.getShantenAfterDiscard());
    }

    @Test
    void testSuggestMovesForOneShantenHand() {
        List<Tile> oneShantenHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.EAST)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(oneShantenHand);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        MoveSuggestion bestMove = suggestions.get(0);
        assertTrue(bestMove.getShantenAfterDiscard() <= 1);
    }

    @Test
    void testGetBestMove() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        MoveSuggestion bestMove = suggestionService.getBestMove(hand);

        assertNotNull(bestMove);
        assertNotNull(bestMove.getDiscardTile());
        assertEquals(0, bestMove.getShantenAfterDiscard());
    }

    @Test
    void testGetTopMoves() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> topMoves = suggestionService.getTopMoves(hand, 3);

        assertNotNull(topMoves);
        assertTrue(topMoves.size() <= 3);
    }

    @Test
    void testSuggestMovesForEmptyHand() {
        List<Tile> emptyHand = Arrays.asList();

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(emptyHand);

        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testMoveSuggestionHasReasoning() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(hand);

        assertFalse(suggestions.isEmpty());
        
        for (MoveSuggestion suggestion : suggestions) {
            assertNotNull(suggestion.getReasoning());
            assertFalse(suggestion.getReasoning().isEmpty());
        }
    }

    @Test
    void testMoveSuggestionHasConfidence() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(hand);

        assertFalse(suggestions.isEmpty());
        
        for (MoveSuggestion suggestion : suggestions) {
            assertTrue(suggestion.getConfidence() >= 0.0);
            assertTrue(suggestion.getConfidence() <= 1.0);
        }
    }

    @Test
    void testExplainMove() {
        // Hand with 4 complete melds + 2 isolated tiles (14 tiles)
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        List<MoveSuggestion> allSuggestions = suggestionService.suggestMoves(hand);
        MoveSuggestion bestMove = suggestionService.getBestMove(hand);
        String explanation = suggestionService.explainMove(bestMove);

        System.out.println("\n=== Test Hand Analysis ===");
        System.out.println("Hand: M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 EAST WEST");
        System.out.println("\nAll move suggestions:");
        for (MoveSuggestion suggestion : allSuggestions) {
            System.out.println(suggestionService.explainMove(suggestion));
        }
        System.out.println("\nBest move explanation:");
        System.out.println(explanation);
        System.out.println("========================\n");

        assertNotNull(explanation);
        assertFalse(explanation.isEmpty());
        assertTrue(explanation.contains("Discard"));
        assertTrue(explanation.contains("Shanten"));
        
        // Verify optimal moves (EAST or WEST)
        MoveSuggestion eastDiscard = allSuggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST)
            .findFirst()
            .orElse(null);
        assertNotNull(eastDiscard, "Should have EAST discard suggestion");
        assertEquals(0, eastDiscard.getShantenAfterDiscard(), "Discarding EAST should result in 0-shanten (tenpai)");
        assertEquals(3, eastDiscard.getUkeireCount(), "Discarding EAST should have 3 tiles ukeire (waiting for EAST)");
        
        MoveSuggestion westDiscard = allSuggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST)
            .findFirst()
            .orElse(null);
        assertNotNull(westDiscard, "Should have WEST discard suggestion");
        assertEquals(0, westDiscard.getShantenAfterDiscard(), "Discarding WEST should result in 0-shanten (tenpai)");
        assertEquals(3, westDiscard.getUkeireCount(), "Discarding WEST should have 3 tiles ukeire (waiting for WEST)");
        
        // Verify suboptimal moves (breaking melds)
        // All of these should result in 1-shanten (3 complete melds + 1 pair + 2 isolated)
        // Currently they show 5-shanten due to chiitoitsu calculation dominating
        
        TileType[] meldTiles = {
            TileType.M1, TileType.M2, TileType.M3,  // M1-M2-M3 sequence
            TileType.M4, TileType.M5, TileType.M6,  // M4-M5-M6 sequence
            TileType.P1,                             // P1-P1-P1 triplet
            TileType.S5, TileType.S6, TileType.S7   // S5-S6-S7 sequence
        };
        
        for (TileType tileType : meldTiles) {
            MoveSuggestion discard = allSuggestions.stream()
                .filter(s -> s.getDiscardTile() == tileType)
                .findFirst()
                .orElse(null);
            assertNotNull(discard, "Should have " + tileType + " discard suggestion");
            assertEquals(1, discard.getShantenAfterDiscard(),
                      "Discarding " + tileType + " should result in 1-shanten");
        }
        
    }

    @Test
    void testSuggestionsSortedByQuality() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.EAST)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(hand);

        assertFalse(suggestions.isEmpty());
        
        for (int i = 0; i < suggestions.size() - 1; i++) {
            MoveSuggestion current = suggestions.get(i);
            MoveSuggestion next = suggestions.get(i + 1);
            
            assertTrue(current.getShantenAfterDiscard() <= next.getShantenAfterDiscard());
        }
    }

    @Test
    void testGetBestMoveForNullHand() {
        MoveSuggestion bestMove = suggestionService.getBestMove(null);
        assertNull(bestMove);
    }

    @Test
    void testOpponentMeldTilesCountedAsVisible() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        List<MoveSuggestion> suggestionsNoMelds = suggestionService.suggestMoves(hand);
        MoveSuggestion eastDiscardNoMelds = suggestionsNoMelds.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST)
            .findFirst().orElse(null);
        assertNotNull(eastDiscardNoMelds);
        assertEquals(3, eastDiscardNoMelds.getUkeireCount(),
            "Without melds, discarding EAST should have 3 ukeire (3 WEST tiles in wall)");

        MeldDTO westPon = new MeldDTO(MeldType.PON,
            List.of(TileType.WEST, TileType.WEST, TileType.WEST));
        PlayerDiscardsDTO opponent = new PlayerDiscardsDTO(
            Wind.SOUTH, Collections.emptyList(), false, List.of(westPon));

        List<MoveSuggestion> suggestionsWithMelds = suggestionService.suggestMoves(hand, List.of(opponent));
        MoveSuggestion eastDiscardWithMelds = suggestionsWithMelds.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST)
            .findFirst().orElse(null);
        assertNotNull(eastDiscardWithMelds);
        assertEquals(0, eastDiscardWithMelds.getUkeireCount(),
            "Opponent's WEST pon locks all 3 remaining WEST tiles, so ukeire should be 0");
    }

    @Test
    void testDefenseChangesRankingWhenShantenAndUkeireTie() {
        // 3 melds + ryanmen 23s + three isolated honors. Discarding any honor
        // leaves the same 1s/4s wait, so shanten and ukeire tie.
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.EAST), new Tile(TileType.WEST), new Tile(TileType.NORTH)
        );

        var eastDiscard = new com.mahjong.dto.DiscardedTileDTO(TileType.EAST, false);
        PlayerDiscardsDTO opponent = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(eastDiscard), false, List.of());

        List<MoveSuggestion> baseline = suggestionService.suggestMoves(hand);
        List<MoveSuggestion> defended = suggestionService.suggestMoves(hand, List.of(opponent));

        MoveSuggestion east = defended.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST).findFirst().orElseThrow();
        MoveSuggestion west = defended.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST).findFirst().orElseThrow();

        assertEquals(east.getShantenAfterDiscard(), west.getShantenAfterDiscard());
        assertTrue(east.getReasoning().contains("Genbutsu"));
        int baselineGap = indexOf(baseline, TileType.EAST) - indexOf(baseline, TileType.WEST);
        int defendedGap = indexOf(defended, TileType.EAST) - indexOf(defended, TileType.WEST);
        assertTrue(defendedGap < baselineGap || indexOf(defended, TileType.EAST) < indexOf(defended, TileType.WEST),
            "Genbutsu EAST vs SOUTH pond must improve EAST's rank relative to WEST");
    }

    private static int indexOf(List<MoveSuggestion> suggestions, TileType tile) {
        for (int i = 0; i < suggestions.size(); i++) {
            if (suggestions.get(i).getDiscardTile() == tile) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    @Test
    void testSeatWindKeepsYakuhaiOnTiebreak() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        List<MoveSuggestion> withWinds = suggestionService.suggestMoves(
            hand, List.of(), List.of(), Wind.EAST, Wind.EAST);
        MoveSuggestion east = withWinds.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST).findFirst().orElseThrow();
        MoveSuggestion west = withWinds.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST).findFirst().orElseThrow();

        assertEquals(0, east.getShantenAfterDiscard());
        assertEquals(0, west.getShantenAfterDiscard());
        assertTrue(withWinds.indexOf(west) < withWinds.indexOf(east),
            "Seat/round EAST is yakuhai and should be kept when WEST is equally efficient");
        assertTrue(east.getReasoning().contains("Yakuhai"));
    }
}
