package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileSuit;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HandAnalyzerTest {

    private HandAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new HandAnalyzer();
    }

    @Test
    void testSortHand() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.P5),
            new Tile(TileType.M1),
            new Tile(TileType.S9),
            new Tile(TileType.EAST)
        );

        List<Tile> sorted = analyzer.sortHand(hand);

        assertEquals(TileType.M1, sorted.get(0).getType());
        assertEquals(TileType.P5, sorted.get(1).getType());
        assertEquals(TileType.S9, sorted.get(2).getType());
        assertEquals(TileType.EAST, sorted.get(3).getType());
    }

    @Test
    void testGetTileCounts() {
        List<Tile> tiles = Arrays.asList(
            new Tile(TileType.M1),
            new Tile(TileType.M1),
            new Tile(TileType.M2),
            new Tile(TileType.P5)
        );

        Map<TileType, Integer> counts = analyzer.getTileCounts(tiles);

        assertEquals(2, counts.get(TileType.M1));
        assertEquals(1, counts.get(TileType.M2));
        assertEquals(1, counts.get(TileType.P5));
    }

    @Test
    void testGroupBySuit() {
        List<Tile> tiles = Arrays.asList(
            new Tile(TileType.M1),
            new Tile(TileType.M2),
            new Tile(TileType.P5),
            new Tile(TileType.EAST)
        );

        Map<TileSuit, List<Tile>> grouped = analyzer.groupBySuit(tiles);

        assertEquals(2, grouped.get(TileSuit.MANZU).size());
        assertEquals(1, grouped.get(TileSuit.PINZU).size());
        assertEquals(1, grouped.get(TileSuit.HONOR).size());
    }

    @Test
    void testIsPair() {
        List<Tile> pair = Arrays.asList(
            new Tile(TileType.M5),
            new Tile(TileType.M5)
        );
        assertTrue(analyzer.isPair(pair));

        List<Tile> notPair = Arrays.asList(
            new Tile(TileType.M5),
            new Tile(TileType.M6)
        );
        assertFalse(analyzer.isPair(notPair));
    }

    @Test
    void testIsSequence() {
        List<Tile> sequence = Arrays.asList(
            new Tile(TileType.M1),
            new Tile(TileType.M2),
            new Tile(TileType.M3)
        );
        assertTrue(analyzer.isSequence(sequence));

        List<Tile> notSequence = Arrays.asList(
            new Tile(TileType.M1),
            new Tile(TileType.M2),
            new Tile(TileType.M4)
        );
        assertFalse(analyzer.isSequence(notSequence));

        List<Tile> honorTiles = Arrays.asList(
            new Tile(TileType.EAST),
            new Tile(TileType.SOUTH),
            new Tile(TileType.WEST)
        );
        assertFalse(analyzer.isSequence(honorTiles));
    }

    @Test
    void testIsTriplet() {
        List<Tile> triplet = Arrays.asList(
            new Tile(TileType.M5),
            new Tile(TileType.M5),
            new Tile(TileType.M5)
        );
        assertTrue(analyzer.isTriplet(triplet));

        List<Tile> notTriplet = Arrays.asList(
            new Tile(TileType.M5),
            new Tile(TileType.M5),
            new Tile(TileType.M6)
        );
        assertFalse(analyzer.isTriplet(notTriplet));
    }

    @Test
    void testIsQuad() {
        List<Tile> quad = Arrays.asList(
            new Tile(TileType.M5),
            new Tile(TileType.M5),
            new Tile(TileType.M5),
            new Tile(TileType.M5)
        );
        assertTrue(analyzer.isQuad(quad));

        List<Tile> notQuad = Arrays.asList(
            new Tile(TileType.M5),
            new Tile(TileType.M5),
            new Tile(TileType.M5)
        );
        assertFalse(analyzer.isQuad(notQuad));
    }

    @Test
    void testIsSevenPairs() {
        List<Tile> sevenPairs = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M1),
            new Tile(TileType.M2), new Tile(TileType.M2),
            new Tile(TileType.M3), new Tile(TileType.M3),
            new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.P2), new Tile(TileType.P2),
            new Tile(TileType.S1), new Tile(TileType.S1),
            new Tile(TileType.EAST), new Tile(TileType.EAST)
        );

        assertTrue(analyzer.isWinningHand(sevenPairs));
    }

    @Test
    void testIsThirteenOrphans() {
        List<Tile> thirteenOrphans = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P9),
            new Tile(TileType.S1), new Tile(TileType.S9),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH),
            new Tile(TileType.WEST), new Tile(TileType.NORTH),
            new Tile(TileType.WHITE), new Tile(TileType.GREEN),
            new Tile(TileType.RED), new Tile(TileType.RED)
        );

        assertTrue(analyzer.isWinningHand(thirteenOrphans));
    }

    @Test
    void testIsStandardWinningHand() {
        List<Tile> standardWin = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S5)
        );

        assertTrue(analyzer.isWinningHand(standardWin));
    }

    @Test
    void testIsNotWinningHand() {
        List<Tile> notWinning = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P2), new Tile(TileType.P3),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        assertFalse(analyzer.isWinningHand(notWinning));
    }

    @Test
    void testGetWaitingTiles() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );

        List<TileType> waiting = analyzer.getWaitingTiles(tenpaiHand);

        assertTrue(waiting.contains(TileType.S5));
    }

    @Test
    void testCountTilesInHand() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1),
            new Tile(TileType.M1),
            new Tile(TileType.M2),
            new Tile(TileType.P5)
        );

        assertEquals(2, analyzer.countTilesInHand(hand, TileType.M1));
        assertEquals(1, analyzer.countTilesInHand(hand, TileType.M2));
        assertEquals(0, analyzer.countTilesInHand(hand, TileType.M3));
    }
}
