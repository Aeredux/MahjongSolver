package com.mahjong.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WindTest {

    @Test
    void testNext() {
        assertEquals(Wind.SOUTH, Wind.EAST.next());
        assertEquals(Wind.WEST, Wind.SOUTH.next());
        assertEquals(Wind.NORTH, Wind.WEST.next());
        assertEquals(Wind.EAST, Wind.NORTH.next());
    }

    @Test
    void testPrevious() {
        assertEquals(Wind.NORTH, Wind.EAST.previous());
        assertEquals(Wind.EAST, Wind.SOUTH.previous());
        assertEquals(Wind.SOUTH, Wind.WEST.previous());
        assertEquals(Wind.WEST, Wind.NORTH.previous());
    }

    @Test
    void testToTileType() {
        assertEquals(TileType.EAST, Wind.EAST.toTileType());
        assertEquals(TileType.SOUTH, Wind.SOUTH.toTileType());
        assertEquals(TileType.WEST, Wind.WEST.toTileType());
        assertEquals(TileType.NORTH, Wind.NORTH.toTileType());
    }
}
