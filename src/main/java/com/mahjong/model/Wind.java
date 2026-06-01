package com.mahjong.model;

public enum Wind {
    EAST,
    SOUTH,
    WEST,
    NORTH;

    public Wind next() {
        return values()[(ordinal() + 1) % 4];
    }

    public Wind previous() {
        return values()[(ordinal() + 3) % 4];
    }

    public TileType toTileType() {
        return switch (this) {
            case EAST -> TileType.EAST;
            case SOUTH -> TileType.SOUTH;
            case WEST -> TileType.WEST;
            case NORTH -> TileType.NORTH;
        };
    }
}
