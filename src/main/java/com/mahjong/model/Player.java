package com.mahjong.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Wind wind;
    private List<Tile> hand;
    private List<Tile> discards;
    private List<Meld> melds;
    private boolean riichi;
    private int score;
    private Tile drawnTile;

    public Player(Wind wind) {
        this.wind = wind;
        this.hand = new ArrayList<>();
        this.discards = new ArrayList<>();
        this.melds = new ArrayList<>();
        this.riichi = false;
        this.score = 25000;
        this.drawnTile = null;
    }

    public int getHandSize() {
        return hand.size() + (drawnTile != null ? 1 : 0);
    }

    public List<Tile> getAllHandTiles() {
        List<Tile> allTiles = new ArrayList<>(hand);
        if (drawnTile != null) {
            allTiles.add(drawnTile);
        }
        return allTiles;
    }

    public boolean hasOpenMelds() {
        return melds.stream().anyMatch(Meld::isOpen);
    }

    public boolean isMenzen() {
        return !hasOpenMelds();
    }
}
