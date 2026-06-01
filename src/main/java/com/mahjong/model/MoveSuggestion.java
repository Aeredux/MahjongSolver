package com.mahjong.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveSuggestion {
    private TileType discardTile;
    private int shantenAfterDiscard;
    private double confidence;
    private String reasoning;
    private int ukeireCount;

    public MoveSuggestion(TileType discardTile, int shantenAfterDiscard) {
        this.discardTile = discardTile;
        this.shantenAfterDiscard = shantenAfterDiscard;
        this.confidence = 0.0;
        this.reasoning = "";
        this.ukeireCount = 0;
    }
}
