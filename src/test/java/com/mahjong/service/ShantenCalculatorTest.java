package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShantenCalculatorTest {

    private ShantenCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShantenCalculator();
    }

    @Test
    void testCompleteHand() {
        List<Tile> completeHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S5)
        );

        assertEquals(-1, calculator.calculateShanten(completeHand));
    }

    @Test
    void testTenpaiHand() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );

        assertEquals(0, calculator.calculateShanten(tenpaiHand));
        assertTrue(calculator.isTenpai(tenpaiHand));
    }

    @Test
    void testOneShantenHand() {
        List<Tile> oneShantenHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        // 3 melds + 2 partial sequences + no pair = 1-shanten (must complete 1 partial AND have a pair source)
        assertEquals(1, calculator.calculateShanten(oneShantenHand));
    }

    @Test
    void testTwoShantenHand() {
        List<Tile> twoShantenHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S1), new Tile(TileType.S3), new Tile(TileType.S5),
            new Tile(TileType.S7)
        );

        // 3 melds + 2 kanchan waits + no pair = 1-shanten
        assertEquals(1, calculator.calculateShanten(twoShantenHand));
    }

    @Test
    void testChiitoitsuTenpai() {
        List<Tile> chiitoitsuTenpai = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M1),
            new Tile(TileType.M2), new Tile(TileType.M2),
            new Tile(TileType.M3), new Tile(TileType.M3),
            new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.P2), new Tile(TileType.P2),
            new Tile(TileType.S1), new Tile(TileType.S1),
            new Tile(TileType.EAST)
        );

        assertEquals(0, calculator.calculateShanten(chiitoitsuTenpai));
    }

    @Test
    void testChiitoitsuOneShantenHand() {
        List<Tile> chiitoitsuOneShanten = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M1),
            new Tile(TileType.M2), new Tile(TileType.M2),
            new Tile(TileType.M3), new Tile(TileType.M3),
            new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.P2), new Tile(TileType.P2),
            new Tile(TileType.S1),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH)
        );

        assertEquals(1, calculator.calculateShanten(chiitoitsuOneShanten));
    }

    @Test
    void testKokushiTenpai() {
        List<Tile> kokushiTenpai = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P9),
            new Tile(TileType.S1), new Tile(TileType.S9),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH),
            new Tile(TileType.WEST), new Tile(TileType.NORTH),
            new Tile(TileType.WHITE), new Tile(TileType.GREEN),
            new Tile(TileType.RED)
        );

        assertEquals(0, calculator.calculateShanten(kokushiTenpai));
    }

    @Test
    void testKokushiOneShantenHand() {
        List<Tile> kokushiOneShanten = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P9),
            new Tile(TileType.S1), new Tile(TileType.S9),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH),
            new Tile(TileType.WEST), new Tile(TileType.NORTH),
            new Tile(TileType.WHITE), new Tile(TileType.GREEN),
            new Tile(TileType.M5)
        );

        assertEquals(1, calculator.calculateShanten(kokushiOneShanten));
    }

    @Test
    void testCalculateShantenAfterDiscard() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        int shantenAfterDiscardS6 = calculator.calculateShantenAfterDiscard(hand, TileType.S6);
        assertEquals(0, shantenAfterDiscardS6);
    }

    @Test
    void testRandomHand() {
        List<Tile> randomHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M3), new Tile(TileType.M5),
            new Tile(TileType.P2), new Tile(TileType.P4), new Tile(TileType.P6),
            new Tile(TileType.S1), new Tile(TileType.S3), new Tile(TileType.S5),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH), new Tile(TileType.WEST),
            new Tile(TileType.WHITE)
        );

        int shanten = calculator.calculateShanten(randomHand);
        assertTrue(shanten >= 0 && shanten <= 8);
    }

    @Test
    void testThreeMeldsNoPairIsOneShantenNotTenpai() {
        // Regression: S1-S9 + M2M3 + WWW (14 tiles) — reported shanten bug
        // Discarding WHITE keeps tenpai (0); discarding any sou tile should give 1-shanten
        List<Tile> hand14 = Arrays.asList(
            new Tile(TileType.S1), new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.S4), new Tile(TileType.S5), new Tile(TileType.S6),
            new Tile(TileType.S7), new Tile(TileType.S8), new Tile(TileType.S9),
            new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.WHITE), new Tile(TileType.WHITE), new Tile(TileType.WHITE)
        );

        // Discard WHITE: S1-S9 + M2M3 + WW = 3 melds + pair(WW) + tatsu(M2M3) = tenpai
        assertEquals(0, calculator.calculateShantenAfterDiscard(hand14, TileType.WHITE));

        // Discard S1: S2-S9 + M2M3 + WWW = 3 melds + 2 tatsu + no pair = 1-shanten
        assertEquals(1, calculator.calculateShantenAfterDiscard(hand14, TileType.S1));

        // Discard S4: S1S2S3 S5S6S7 S8S9 + M2M3 + WWW = same structure = 1-shanten
        assertEquals(1, calculator.calculateShantenAfterDiscard(hand14, TileType.S4));
    }

    @Test
    void testEmptyHand() {
        List<Tile> emptyHand = Arrays.asList();
        assertEquals(8, calculator.calculateShanten(emptyHand));
    }

    @Test
    void testAllPairs() {
        List<Tile> allPairs = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M1),
            new Tile(TileType.M2), new Tile(TileType.M2),
            new Tile(TileType.M3), new Tile(TileType.M3),
            new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.P2), new Tile(TileType.P2),
            new Tile(TileType.S1), new Tile(TileType.S1),
            new Tile(TileType.EAST)
        );

        int shanten = calculator.calculateShanten(allPairs);
        assertTrue(shanten <= 1);
    }

    @Test
    void testIsTenpai() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );

        assertTrue(calculator.isTenpai(tenpaiHand));

        List<Tile> notTenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M3), new Tile(TileType.M5),
            new Tile(TileType.P2), new Tile(TileType.P4), new Tile(TileType.P6),
            new Tile(TileType.S1), new Tile(TileType.S3), new Tile(TileType.S5),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH), new Tile(TileType.WEST),
            new Tile(TileType.WHITE)
        );

        assertFalse(calculator.isTenpai(notTenpaiHand));
    }
}
