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

        assertEquals(2, calculator.calculateShanten(twoShantenHand));
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
