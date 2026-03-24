package com.mahjong.service;

import com.mahjong.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class WallService {
    
    private static final Logger logger = LoggerFactory.getLogger(WallService.class);
    private static final int DEAD_WALL_SIZE = 14;
    private static final int DORA_INDICATORS_COUNT = 5;

    public void initializeWall(GameState gameState) {
        logger.debug("Initializing wall for game {}", gameState.getGameId());
        
        List<Tile> wall = createFullTileSet();
        Collections.shuffle(wall);
        
        gameState.setWall(wall);
        gameState.setWallIndex(0);
        
        setupDeadWall(gameState);
        
        logger.debug("Wall initialized with {} tiles", wall.size());
    }

    private List<Tile> createFullTileSet() {
        List<Tile> tiles = new ArrayList<>();
        
        for (TileType type : TileType.values()) {
            for (int i = 0; i < 4; i++) {
                boolean isRed = false;
                if ((type == TileType.M5 || type == TileType.P5 || type == TileType.S5) && i == 0) {
                    isRed = true;
                }
                tiles.add(new Tile(type, isRed));
            }
        }
        
        return tiles;
    }

    private void setupDeadWall(GameState gameState) {
        List<Tile> wall = gameState.getWall();
        int wallSize = wall.size();
        
        List<Tile> deadWall = new ArrayList<>();
        for (int i = wallSize - DEAD_WALL_SIZE; i < wallSize; i++) {
            deadWall.add(wall.get(i));
        }
        gameState.setDeadWall(deadWall);
        
        List<Tile> doraIndicators = new ArrayList<>();
        doraIndicators.add(deadWall.get(4));
        gameState.setDoraIndicators(doraIndicators);
        
        List<Tile> uraDoraIndicators = new ArrayList<>();
        uraDoraIndicators.add(deadWall.get(9));
        gameState.setUraDoraIndicators(uraDoraIndicators);
        
        logger.debug("Dead wall set up with {} tiles, initial dora indicator: {}", 
                    DEAD_WALL_SIZE, doraIndicators.get(0));
    }

    public void dealInitialHands(GameState gameState) {
        logger.debug("Dealing initial hands for game {}", gameState.getGameId());
        
        int tilesPerPlayer = 13;
        
        for (Wind wind : Wind.values()) {
            Player player = gameState.getPlayer(wind);
            List<Tile> hand = new ArrayList<>();
            
            for (int i = 0; i < tilesPerPlayer; i++) {
                Tile tile = drawTileFromWall(gameState);
                if (tile != null) {
                    hand.add(tile);
                }
            }
            
            player.setHand(hand);
            logger.debug("Dealt {} tiles to {} player", hand.size(), wind);
        }
    }

    public Tile drawTileFromWall(GameState gameState) {
        if (gameState.isWallEmpty()) {
            logger.warn("Attempted to draw from empty wall");
            return null;
        }
        
        List<Tile> wall = gameState.getWall();
        int index = gameState.getWallIndex();
        
        int deadWallStart = wall.size() - DEAD_WALL_SIZE;
        if (index >= deadWallStart) {
            logger.warn("Wall exhausted, cannot draw more tiles");
            return null;
        }
        
        Tile tile = wall.get(index);
        gameState.setWallIndex(index + 1);
        
        logger.debug("Drew tile {} from wall, {} tiles remaining", 
                    tile, gameState.getTilesRemaining());
        
        return tile;
    }

    public Tile drawReplacementTile(GameState gameState) {
        List<Tile> deadWall = gameState.getDeadWall();
        int replacementIndex = DEAD_WALL_SIZE - 1 - gameState.getDoraIndicators().size();
        
        if (replacementIndex < 0) {
            logger.warn("No replacement tiles available");
            return null;
        }
        
        Tile tile = deadWall.get(replacementIndex);
        logger.debug("Drew replacement tile {} from dead wall", tile);
        
        return tile;
    }

    public void revealNextDoraIndicator(GameState gameState) {
        List<Tile> doraIndicators = gameState.getDoraIndicators();
        
        if (doraIndicators.size() >= DORA_INDICATORS_COUNT) {
            logger.warn("Maximum dora indicators already revealed");
            return;
        }
        
        List<Tile> deadWall = gameState.getDeadWall();
        int nextDoraIndex = 4 - doraIndicators.size();
        
        Tile nextDora = deadWall.get(nextDoraIndex);
        doraIndicators.add(nextDora);
        
        List<Tile> uraDoraIndicators = gameState.getUraDoraIndicators();
        int nextUraDoraIndex = 9 - uraDoraIndicators.size();
        Tile nextUraDora = deadWall.get(nextUraDoraIndex);
        uraDoraIndicators.add(nextUraDora);
        
        logger.debug("Revealed dora indicator {}: {}", doraIndicators.size(), nextDora);
    }

    public List<Tile> getActualDoraTiles(GameState gameState) {
        List<Tile> doraTiles = new ArrayList<>();
        
        for (Tile indicator : gameState.getDoraIndicators()) {
            doraTiles.add(getNextTile(indicator));
        }
        
        return doraTiles;
    }

    private Tile getNextTile(Tile indicator) {
        TileType indicatorType = indicator.getType();
        TileSuit suit = indicatorType.getSuit();
        int value = indicatorType.getValue();
        
        TileType nextType;
        
        if (suit == TileSuit.HONOR) {
            if (value <= 4) {
                nextType = switch (value) {
                    case 1 -> TileType.SOUTH;
                    case 2 -> TileType.WEST;
                    case 3 -> TileType.NORTH;
                    case 4 -> TileType.EAST;
                    default -> indicatorType;
                };
            } else {
                nextType = switch (value) {
                    case 5 -> TileType.GREEN;
                    case 6 -> TileType.RED;
                    case 7 -> TileType.WHITE;
                    default -> indicatorType;
                };
            }
        } else {
            int nextValue = (value % 9) + 1;
            String suitPrefix = switch (suit) {
                case MANZU -> "M";
                case PINZU -> "P";
                case SOUZU -> "S";
                default -> "";
            };
            nextType = TileType.valueOf(suitPrefix + nextValue);
        }
        
        return new Tile(nextType);
    }
}
