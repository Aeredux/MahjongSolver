package com.mahjong.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    @Test
    void testTileCreation() {
        Tile tile = new Tile(TileType.M5);
        assertEquals(TileType.M5, tile.getType());
        assertFalse(tile.isRed());
    }

    @Test
    void testRedTile() {
        Tile redTile = new Tile(TileType.M5, true);
        assertTrue(redTile.isRed());
        assertEquals(TileType.M5, redTile.getType());
    }

    @Test
    void testTileSuit() {
        Tile manzuTile = new Tile(TileType.M5);
        assertEquals(TileSuit.MANZU, manzuTile.getSuit());

        Tile pinzuTile = new Tile(TileType.P3);
        assertEquals(TileSuit.PINZU, pinzuTile.getSuit());

        Tile souzuTile = new Tile(TileType.S7);
        assertEquals(TileSuit.SOUZU, souzuTile.getSuit());

        Tile honorTile = new Tile(TileType.EAST);
        assertEquals(TileSuit.HONOR, honorTile.getSuit());
    }

    @Test
    void testTileValue() {
        Tile tile = new Tile(TileType.M5);
        assertEquals(5, tile.getValue());

        Tile eastTile = new Tile(TileType.EAST);
        assertEquals(1, eastTile.getValue());
    }

    @Test
    void testTerminalTiles() {
        assertTrue(new Tile(TileType.M1).isTerminal());
        assertTrue(new Tile(TileType.M9).isTerminal());
        assertTrue(new Tile(TileType.P1).isTerminal());
        assertTrue(new Tile(TileType.P9).isTerminal());
        assertTrue(new Tile(TileType.S1).isTerminal());
        assertTrue(new Tile(TileType.S9).isTerminal());

        assertFalse(new Tile(TileType.M5).isTerminal());
        assertFalse(new Tile(TileType.EAST).isTerminal());
    }

    @Test
    void testHonorTiles() {
        assertTrue(new Tile(TileType.EAST).isHonor());
        assertTrue(new Tile(TileType.SOUTH).isHonor());
        assertTrue(new Tile(TileType.WEST).isHonor());
        assertTrue(new Tile(TileType.NORTH).isHonor());
        assertTrue(new Tile(TileType.WHITE).isHonor());
        assertTrue(new Tile(TileType.GREEN).isHonor());
        assertTrue(new Tile(TileType.RED).isHonor());

        assertFalse(new Tile(TileType.M5).isHonor());
    }

    @Test
    void testWindTiles() {
        assertTrue(new Tile(TileType.EAST).isWind());
        assertTrue(new Tile(TileType.SOUTH).isWind());
        assertTrue(new Tile(TileType.WEST).isWind());
        assertTrue(new Tile(TileType.NORTH).isWind());

        assertFalse(new Tile(TileType.WHITE).isWind());
        assertFalse(new Tile(TileType.M5).isWind());
    }

    @Test
    void testDragonTiles() {
        assertTrue(new Tile(TileType.WHITE).isDragon());
        assertTrue(new Tile(TileType.GREEN).isDragon());
        assertTrue(new Tile(TileType.RED).isDragon());

        assertFalse(new Tile(TileType.EAST).isDragon());
        assertFalse(new Tile(TileType.M5).isDragon());
    }

    @Test
    void testSimpleTiles() {
        assertTrue(new Tile(TileType.M2).isSimple());
        assertTrue(new Tile(TileType.M5).isSimple());
        assertTrue(new Tile(TileType.P3).isSimple());
        assertTrue(new Tile(TileType.S8).isSimple());

        assertFalse(new Tile(TileType.M1).isSimple());
        assertFalse(new Tile(TileType.M9).isSimple());
        assertFalse(new Tile(TileType.EAST).isSimple());
    }

    @Test
    void testTerminalOrHonor() {
        assertTrue(new Tile(TileType.M1).isTerminalOrHonor());
        assertTrue(new Tile(TileType.M9).isTerminalOrHonor());
        assertTrue(new Tile(TileType.EAST).isTerminalOrHonor());
        assertTrue(new Tile(TileType.WHITE).isTerminalOrHonor());

        assertFalse(new Tile(TileType.M5).isTerminalOrHonor());
    }

    @Test
    void testToString() {
        Tile normalTile = new Tile(TileType.M5);
        assertEquals("M5", normalTile.toString());

        Tile redTile = new Tile(TileType.M5, true);
        assertEquals("M5r", redTile.toString());
    }
}
