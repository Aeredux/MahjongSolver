package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class ShantenCalculatorManualTest {

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Test
    void testFourMeldsHand() {
        // Hand with 4 complete melds + 2 isolated tiles (14 tiles total)
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),  // sequence
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),  // sequence
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),  // triplet
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),  // sequence
            new Tile(TileType.EAST), new Tile(TileType.WEST)                       // isolated
        );

        System.out.println("\n=== Testing 4 Complete Melds + 2 Isolated ===");
        System.out.println("Hand (14 tiles): M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 EAST WEST");
        int shanten14 = shantenCalculator.calculateShanten(hand);
        System.out.println("14-tile hand shanten: " + shanten14);

        // After discarding EAST (13 tiles)
        List<Tile> handAfterEast = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.WEST)
        );
        System.out.println("\nAfter discarding EAST (13 tiles): M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 WEST");
        int shantenAfterEast = shantenCalculator.calculateShanten(handAfterEast);
        System.out.println("Shanten: " + shantenAfterEast);
        System.out.println("Expected: 0 (tenpai, waiting for WEST to complete pair)");

        // After discarding WEST (13 tiles)
        List<Tile> handAfterWest = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST)
        );
        System.out.println("\nAfter discarding WEST (13 tiles): M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 EAST");
        int shantenAfterWest = shantenCalculator.calculateShanten(handAfterWest);
        System.out.println("Shanten: " + shantenAfterWest);
        System.out.println("Expected: 0 (tenpai, waiting for EAST to complete pair)");
        System.out.println("===========================================\n");
    }

    @Test
    void testCompletedHand() {
        // Completed hand: 4 melds + 1 pair (14 tiles)
        List<Tile> completedHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.EAST)
        );

        System.out.println("\n=== Testing Completed Hand ===");
        System.out.println("Hand (14 tiles): M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 EAST EAST");
        int shanten = shantenCalculator.calculateShanten(completedHand);
        System.out.println("Shanten: " + shanten);
        System.out.println("Expected: -1 (completed hand)");
        System.out.println("===============================\n");
    }
}
