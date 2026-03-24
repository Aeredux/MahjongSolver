package com.mahjong.model;

public enum TileType {
    M1(TileSuit.MANZU, 1),
    M2(TileSuit.MANZU, 2),
    M3(TileSuit.MANZU, 3),
    M4(TileSuit.MANZU, 4),
    M5(TileSuit.MANZU, 5),
    M6(TileSuit.MANZU, 6),
    M7(TileSuit.MANZU, 7),
    M8(TileSuit.MANZU, 8),
    M9(TileSuit.MANZU, 9),
    
    P1(TileSuit.PINZU, 1),
    P2(TileSuit.PINZU, 2),
    P3(TileSuit.PINZU, 3),
    P4(TileSuit.PINZU, 4),
    P5(TileSuit.PINZU, 5),
    P6(TileSuit.PINZU, 6),
    P7(TileSuit.PINZU, 7),
    P8(TileSuit.PINZU, 8),
    P9(TileSuit.PINZU, 9),
    
    S1(TileSuit.SOUZU, 1),
    S2(TileSuit.SOUZU, 2),
    S3(TileSuit.SOUZU, 3),
    S4(TileSuit.SOUZU, 4),
    S5(TileSuit.SOUZU, 5),
    S6(TileSuit.SOUZU, 6),
    S7(TileSuit.SOUZU, 7),
    S8(TileSuit.SOUZU, 8),
    S9(TileSuit.SOUZU, 9),
    
    EAST(TileSuit.HONOR, 1),
    SOUTH(TileSuit.HONOR, 2),
    WEST(TileSuit.HONOR, 3),
    NORTH(TileSuit.HONOR, 4),
    WHITE(TileSuit.HONOR, 5),
    GREEN(TileSuit.HONOR, 6),
    RED(TileSuit.HONOR, 7);

    private final TileSuit suit;
    private final int value;

    TileType(TileSuit suit, int value) {
        this.suit = suit;
        this.value = value;
    }

    public TileSuit getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public boolean isTerminal() {
        return (suit != TileSuit.HONOR) && (value == 1 || value == 9);
    }

    public boolean isHonor() {
        return suit == TileSuit.HONOR;
    }

    public boolean isWind() {
        return suit == TileSuit.HONOR && value <= 4;
    }

    public boolean isDragon() {
        return suit == TileSuit.HONOR && value > 4;
    }

    public boolean isSimple() {
        return !isTerminal() && !isHonor();
    }
}
