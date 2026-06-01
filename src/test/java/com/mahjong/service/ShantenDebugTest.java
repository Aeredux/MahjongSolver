package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class ShantenDebugTest {

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Test
    void debugFourteenTileHand() {
        // 14-tile hand with 4 complete melds
        List<Tile> hand14 = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        System.out.println("\n=== 14-Tile Hand Analysis ===");
        System.out.println("Hand: M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 EAST WEST");
        System.out.println("Structure: 4 complete melds (12 tiles) + 2 isolated tiles");
        
        int shanten14 = shantenCalculator.calculateShanten(hand14);
        System.out.println("\nCalculated shanten: " + shanten14);
        System.out.println("Expected: Should recognize this as having 4 complete melds");
        System.out.println("For 14 tiles, this should be close to tenpai (0-1 shanten)");
        
        // Check standard vs chiitoitsu vs kokushi
        System.out.println("\nNote: A 14-tile hand is not a standard mahjong state.");
        System.out.println("The calculator should handle it gracefully.");
        System.out.println("With 4 complete melds already formed, discarding any of the");
        System.out.println("2 isolated tiles should result in 0-shanten (tenpai).");
        System.out.println("===============================\n");
    }
}
