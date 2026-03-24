package com.mahjong.service;

import com.mahjong.model.MoveSuggestion;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
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
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        MoveSuggestion bestMove = suggestionService.getBestMove(hand);
        String explanation = suggestionService.explainMove(bestMove);

        assertNotNull(explanation);
        assertFalse(explanation.isEmpty());
        assertTrue(explanation.contains("Discard"));
        assertTrue(explanation.contains("Shanten"));
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
}
