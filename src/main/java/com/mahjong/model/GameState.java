package com.mahjong.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameState {
    private String gameId;
    private Wind roundWind;
    private int roundNumber;
    private int honbaSticks;
    private int riichiSticks;
    private Map<Wind, Player> players;
    private Wind currentPlayerWind;
    private List<Tile> wall;
    private List<Tile> deadWall;
    private List<Tile> doraIndicators;
    private List<Tile> uraDoraIndicators;
    private int wallIndex;

    public GameState(String gameId) {
        this.gameId = gameId;
        this.roundWind = Wind.EAST;
        this.roundNumber = 1;
        this.honbaSticks = 0;
        this.riichiSticks = 0;
        this.players = new HashMap<>();
        this.currentPlayerWind = Wind.EAST;
        this.wall = new ArrayList<>();
        this.deadWall = new ArrayList<>();
        this.doraIndicators = new ArrayList<>();
        this.uraDoraIndicators = new ArrayList<>();
        this.wallIndex = 0;

        for (Wind wind : Wind.values()) {
            players.put(wind, new Player(wind));
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerWind);
    }

    public Player getPlayer(Wind wind) {
        return players.get(wind);
    }

    public void nextPlayer() {
        currentPlayerWind = currentPlayerWind.next();
    }

    public int getTilesRemaining() {
        return wall.size() - wallIndex;
    }

    public boolean isWallEmpty() {
        return getTilesRemaining() <= 0;
    }

    public List<Tile> getVisibleDoraIndicators() {
        return new ArrayList<>(doraIndicators);
    }
}
