package com.mahjong.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tile {
    private TileType type;
    private boolean isRed;

    public Tile(TileType type) {
        this.type = type;
        this.isRed = false;
    }

    public TileSuit getSuit() {
        return type.getSuit();
    }

    public int getValue() {
        return type.getValue();
    }

    public boolean isTerminal() {
        return type.isTerminal();
    }

    public boolean isHonor() {
        return type.isHonor();
    }

    public boolean isWind() {
        return type.isWind();
    }

    public boolean isDragon() {
        return type.isDragon();
    }

    public boolean isSimple() {
        return type.isSimple();
    }

    public boolean isTerminalOrHonor() {
        return isTerminal() || isHonor();
    }

    @Override
    public String toString() {
        String redIndicator = isRed ? "r" : "";
        return type.name() + redIndicator;
    }
}
