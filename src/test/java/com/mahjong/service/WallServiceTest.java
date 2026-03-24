package com.mahjong.service;

import com.mahjong.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WallServiceTest {

    private WallService wallService;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        wallService = new WallService();
        gameState = new GameState("test-game");
    }

    @Test
    void testInitializeWall() {
        wallService.initializeWall(gameState);
        
        assertNotNull(gameState.getWall());
        assertEquals(136, gameState.getWall().size());
        assertEquals(0, gameState.getWallIndex());
        assertNotNull(gameState.getDeadWall());
        assertEquals(14, gameState.getDeadWall().size());
        assertNotNull(gameState.getDoraIndicators());
        assertEquals(1, gameState.getDoraIndicators().size());
        assertNotNull(gameState.getUraDoraIndicators());
        assertEquals(1, gameState.getUraDoraIndicators().size());
    }

    @Test
    void testDealInitialHands() {
        wallService.initializeWall(gameState);
        wallService.dealInitialHands(gameState);
        
        for (Wind wind : Wind.values()) {
            Player player = gameState.getPlayer(wind);
            assertEquals(13, player.getHand().size());
        }
        
        assertEquals(52, gameState.getWallIndex());
    }

    @Test
    void testDrawTileFromWall() {
        wallService.initializeWall(gameState);
        
        int initialRemaining = gameState.getTilesRemaining();
        Tile tile = wallService.drawTileFromWall(gameState);
        
        assertNotNull(tile);
        assertEquals(1, gameState.getWallIndex());
        assertEquals(initialRemaining - 1, gameState.getTilesRemaining());
    }

    @Test
    void testDrawTileFromEmptyWall() {
        wallService.initializeWall(gameState);
        gameState.setWallIndex(122);
        
        Tile tile = wallService.drawTileFromWall(gameState);
        assertNull(tile);
    }

    @Test
    void testDrawReplacementTile() {
        wallService.initializeWall(gameState);
        
        Tile replacementTile = wallService.drawReplacementTile(gameState);
        assertNotNull(replacementTile);
    }

    @Test
    void testRevealNextDoraIndicator() {
        wallService.initializeWall(gameState);
        
        assertEquals(1, gameState.getDoraIndicators().size());
        
        wallService.revealNextDoraIndicator(gameState);
        assertEquals(2, gameState.getDoraIndicators().size());
        assertEquals(2, gameState.getUraDoraIndicators().size());
        
        wallService.revealNextDoraIndicator(gameState);
        assertEquals(3, gameState.getDoraIndicators().size());
    }

    @Test
    void testRevealMaxDoraIndicators() {
        wallService.initializeWall(gameState);
        
        for (int i = 0; i < 5; i++) {
            wallService.revealNextDoraIndicator(gameState);
        }
        
        assertEquals(5, gameState.getDoraIndicators().size());
        
        wallService.revealNextDoraIndicator(gameState);
        assertEquals(5, gameState.getDoraIndicators().size());
    }

    @Test
    void testGetActualDoraTiles() {
        wallService.initializeWall(gameState);
        
        List<Tile> doraTiles = wallService.getActualDoraTiles(gameState);
        assertNotNull(doraTiles);
        assertEquals(1, doraTiles.size());
    }

    @Test
    void testRedTilesInWall() {
        wallService.initializeWall(gameState);
        
        long redTileCount = gameState.getWall().stream()
            .filter(Tile::isRed)
            .count();
        
        assertEquals(3, redTileCount);
    }

    @Test
    void testTileDistribution() {
        wallService.initializeWall(gameState);
        
        for (TileType type : TileType.values()) {
            long count = gameState.getWall().stream()
                .filter(tile -> tile.getType() == type)
                .count();
            
            assertEquals(4, count);
        }
    }
}
