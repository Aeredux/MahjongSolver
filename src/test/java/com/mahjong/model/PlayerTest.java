package com.mahjong.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(Wind.EAST);
    }

    @Test
    void testPlayerCreation() {
        assertEquals(Wind.EAST, player.getWind());
        assertEquals(25000, player.getScore());
        assertFalse(player.isRiichi());
        assertNotNull(player.getHand());
        assertNotNull(player.getDiscards());
        assertNotNull(player.getMelds());
        assertNull(player.getDrawnTile());
    }

    @Test
    void testGetHandSize() {
        assertEquals(0, player.getHandSize());

        player.getHand().add(new Tile(TileType.M1));
        player.getHand().add(new Tile(TileType.M2));
        assertEquals(2, player.getHandSize());

        player.setDrawnTile(new Tile(TileType.M3));
        assertEquals(3, player.getHandSize());
    }

    @Test
    void testGetAllHandTiles() {
        player.getHand().add(new Tile(TileType.M1));
        player.getHand().add(new Tile(TileType.M2));
        
        List<Tile> allTiles = player.getAllHandTiles();
        assertEquals(2, allTiles.size());

        player.setDrawnTile(new Tile(TileType.M3));
        allTiles = player.getAllHandTiles();
        assertEquals(3, allTiles.size());
    }

    @Test
    void testHasOpenMelds() {
        assertFalse(player.hasOpenMelds());

        Meld closedKan = new Meld(MeldType.KAN_CLOSED, 
            Arrays.asList(new Tile(TileType.M1), new Tile(TileType.M1), 
                         new Tile(TileType.M1), new Tile(TileType.M1)), 
            null);
        player.getMelds().add(closedKan);
        assertFalse(player.hasOpenMelds());

        Meld pon = new Meld(MeldType.PON, 
            Arrays.asList(new Tile(TileType.M2), new Tile(TileType.M2), new Tile(TileType.M2)), 
            Wind.SOUTH);
        player.getMelds().add(pon);
        assertTrue(player.hasOpenMelds());
    }

    @Test
    void testIsMenzen() {
        assertTrue(player.isMenzen());

        Meld closedKan = new Meld(MeldType.KAN_CLOSED, 
            Arrays.asList(new Tile(TileType.M1), new Tile(TileType.M1), 
                         new Tile(TileType.M1), new Tile(TileType.M1)), 
            null);
        player.getMelds().add(closedKan);
        assertTrue(player.isMenzen());

        Meld chi = new Meld(MeldType.CHI, 
            Arrays.asList(new Tile(TileType.M2), new Tile(TileType.M3), new Tile(TileType.M4)), 
            Wind.NORTH);
        player.getMelds().add(chi);
        assertFalse(player.isMenzen());
    }
}
