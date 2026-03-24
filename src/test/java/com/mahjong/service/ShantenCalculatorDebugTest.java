package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShantenCalculatorDebugTest {
    
    private ShantenCalculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new ShantenCalculator();
    }
    
    @Test
    void testProblemHand() {
        // This hand has NO pair - should be tenpai (0-shanten), not winning (-1)
        // M1 M2 M3 M4 M5 M6 M7 M8 P1 P1 P1 S5 S6 S7
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7)
        );
        
        int shanten = calculator.calculateShanten(hand);
        
        System.out.println("\n=== Debug Problem Hand ===");
        System.out.println("Hand: M1 M2 M3 M4 M5 M6 M7 M8 P1 P1 P1 S5 S6 S7");
        System.out.println("Tile counts:");
        System.out.println("  M1:1 M2:1 M3:1 M4:1 M5:1 M6:1 M7:1 M8:1");
        System.out.println("  P1:3");
        System.out.println("  S5:1 S6:1 S7:1");
        System.out.println("\nPossible melds:");
        System.out.println("  M1-M2-M3 (sequence)");
        System.out.println("  M4-M5-M6 (sequence)");
        System.out.println("  M7-M8-? (incomplete - needs M6 or M9)");
        System.out.println("  P1-P1-P1 (triplet)");
        System.out.println("  S5-S6-S7 (sequence)");
        System.out.println("\nTotal: 4 complete melds, 0 pairs");
        System.out.println("This is TENPAI (0-shanten), waiting for a tile to form a pair");
        System.out.println("\nCalculated shanten: " + shanten);
        System.out.println("Expected: 0");
        System.out.println("========================\n");
        
        // This hand should be 0-shanten (tenpai), not -1 (winning)
        // It has 4 complete melds but NO pair
        assertEquals(0, shanten, "Hand with 4 melds but no pair should be tenpai (0-shanten)");
    }
}
