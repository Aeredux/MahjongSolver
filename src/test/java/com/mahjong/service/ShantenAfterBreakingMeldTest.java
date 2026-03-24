package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class ShantenAfterBreakingMeldTest {

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Test
    void testDiscardFromTriplet() {
        // After discarding P1 from the triplet
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1),  // Now a pair instead of triplet
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        System.out.println("\n=== After Discarding P1 from Triplet ===");
        System.out.println("Hand (13 tiles): M1 M2 M3 M4 M5 M6 P1 P1 S5 S6 S7 EAST WEST");
        System.out.println("Structure:");
        System.out.println("  - M1-M2-M3 (complete sequence)");
        System.out.println("  - M4-M5-M6 (complete sequence)");
        System.out.println("  - S5-S6-S7 (complete sequence)");
        System.out.println("  - P1-P1 (pair)");
        System.out.println("  - EAST, WEST (2 isolated tiles)");
        
        int shanten = shantenCalculator.calculateShanten(hand);
        System.out.println("\nCalculated shanten: " + shanten);
        System.out.println("Expected: 1-shanten");
        System.out.println("Reasoning: 3 complete melds + 1 pair + 2 isolated tiles");
        System.out.println("           Need to form 1 more meld from the 2 isolated tiles");
        System.out.println("           = 1-shanten");
        System.out.println("=========================================\n");
    }
}
