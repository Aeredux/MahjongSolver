package com.mahjong.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState("test-game-1");
    }

    @Test
    void testGameStateCreation() {
        assertEquals("test-game-1", gameState.getGameId());
        assertEquals(Wind.EAST, gameState.getRoundWind());
        assertEquals(1, gameState.getRoundNumber());
        assertEquals(0, gameState.getHonbaSticks());
        assertEquals(0, gameState.getRiichiSticks());
        assertEquals(Wind.EAST, gameState.getCurrentPlayerWind());
        assertNotNull(gameState.getPlayers());
        assertEquals(4, gameState.getPlayers().size());
    }

    @Test
    void testGetCurrentPlayer() {
        Player currentPlayer = gameState.getCurrentPlayer();
        assertNotNull(currentPlayer);
        assertEquals(Wind.EAST, currentPlayer.getWind());
    }

    @Test
    void testGetPlayer() {
        Player eastPlayer = gameState.getPlayer(Wind.EAST);
        assertNotNull(eastPlayer);
        assertEquals(Wind.EAST, eastPlayer.getWind());

        Player southPlayer = gameState.getPlayer(Wind.SOUTH);
        assertNotNull(southPlayer);
        assertEquals(Wind.SOUTH, southPlayer.getWind());
    }

    @Test
    void testNextPlayer() {
        assertEquals(Wind.EAST, gameState.getCurrentPlayerWind());
        
        gameState.nextPlayer();
        assertEquals(Wind.SOUTH, gameState.getCurrentPlayerWind());
        
        gameState.nextPlayer();
        assertEquals(Wind.WEST, gameState.getCurrentPlayerWind());
        
        gameState.nextPlayer();
        assertEquals(Wind.NORTH, gameState.getCurrentPlayerWind());
        
        gameState.nextPlayer();
        assertEquals(Wind.EAST, gameState.getCurrentPlayerWind());
    }

    @Test
    void testGetTilesRemaining() {
        assertEquals(0, gameState.getTilesRemaining());

        for (int i = 0; i < 70; i++) {
            gameState.getWall().add(new Tile(TileType.M1));
        }
        assertEquals(70, gameState.getTilesRemaining());

        gameState.setWallIndex(10);
        assertEquals(60, gameState.getTilesRemaining());
    }

    @Test
    void testIsWallEmpty() {
        assertTrue(gameState.isWallEmpty());

        gameState.getWall().add(new Tile(TileType.M1));
        assertFalse(gameState.isWallEmpty());

        gameState.setWallIndex(1);
        assertTrue(gameState.isWallEmpty());
    }

    @Test
    void testGetVisibleDoraIndicators() {
        assertTrue(gameState.getVisibleDoraIndicators().isEmpty());

        gameState.getDoraIndicators().add(new Tile(TileType.M1));
        assertEquals(1, gameState.getVisibleDoraIndicators().size());
    }
}
