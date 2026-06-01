package com.mahjong.service;

import com.mahjong.entity.GameHistory;
import com.mahjong.model.MoveSuggestion;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GameHistoryServiceTest {

    @Autowired
    private GameHistoryService gameHistoryService;

    @Autowired
    private MoveSuggestionService moveSuggestionService;

    private List<TileType> sampleHand() {
        return Arrays.asList(
            TileType.M1, TileType.M2, TileType.M3,
            TileType.M4, TileType.M5, TileType.M6,
            TileType.M7, TileType.M8, TileType.M9,
            TileType.P1, TileType.P1, TileType.P1,
            TileType.S5, TileType.S6
        );
    }

    @Test
    void testSaveGameHistoryPersistsRecord() {
        List<TileType> hand = sampleHand();
        List<Tile> tiles = hand.stream().map(Tile::new).toList();
        List<MoveSuggestion> suggestions = moveSuggestionService.suggestMoves(tiles);

        GameHistory saved = gameHistoryService.saveGameHistory(hand, TileType.S6, -1, suggestions);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertFalse(saved.getHandTiles().isEmpty());
        assertEquals("S6", saved.getDrawnTile());
        assertEquals(-1, saved.getCurrentShanten());
        assertFalse(suggestions.isEmpty());
        assertEquals(suggestions.size(), saved.getSuggestionCount());
    }

    @Test
    void testSaveGameHistoryWithNoDrawnTile() {
        List<TileType> hand = sampleHand();
        List<Tile> tiles = hand.stream().map(Tile::new).toList();
        List<MoveSuggestion> suggestions = moveSuggestionService.suggestMoves(tiles);

        GameHistory saved = gameHistoryService.saveGameHistory(hand, null, 0, suggestions);

        assertNotNull(saved.getId());
        assertNull(saved.getDrawnTile());
    }

    @Test
    void testSaveGameHistoryWithEmptySuggestionsHasNoBestDiscard() {
        List<TileType> hand = sampleHand();

        GameHistory saved = gameHistoryService.saveGameHistory(hand, null, 2, List.of());

        assertNotNull(saved.getId());
        assertNull(saved.getBestDiscard());
        assertEquals(0, saved.getSuggestionCount());
    }

    @Test
    void testFindByIdReturnsRecord() {
        List<TileType> hand = sampleHand();
        GameHistory saved = gameHistoryService.saveGameHistory(hand, null, 1, List.of());

        Optional<GameHistory> found = gameHistoryService.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testFindAllReturnsAllRecords() {
        List<TileType> hand = sampleHand();
        gameHistoryService.saveGameHistory(hand, null, 1, List.of());
        gameHistoryService.saveGameHistory(hand, TileType.M1, 0, List.of());

        List<GameHistory> all = gameHistoryService.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void testFindRecentRespectsLimit() {
        List<TileType> hand = sampleHand();
        for (int i = 0; i < 5; i++) {
            gameHistoryService.saveGameHistory(hand, null, i, List.of());
        }

        List<GameHistory> recent = gameHistoryService.findRecent(3);

        assertEquals(3, recent.size());
    }

    @Test
    void testHandTilesSerializedAsCommaSeparated() {
        List<TileType> hand = Arrays.asList(TileType.M1, TileType.M2, TileType.M3);
        GameHistory saved = gameHistoryService.saveGameHistory(hand, null, 5, List.of());

        assertEquals("M1,M2,M3", saved.getHandTiles());
    }

    @Test
    void testBestDiscardAndConfidenceStoredFromTopSuggestion() {
        List<TileType> hand = sampleHand();
        List<Tile> tiles = hand.stream().map(Tile::new).toList();
        List<MoveSuggestion> suggestions = moveSuggestionService.suggestMoves(tiles);

        GameHistory saved = gameHistoryService.saveGameHistory(hand, null, 0, suggestions);

        assertNotNull(saved.getBestDiscard());
        assertTrue(saved.getBestConfidence() >= 0.0 && saved.getBestConfidence() <= 1.0);
    }
}
