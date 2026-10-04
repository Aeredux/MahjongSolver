package com.mahjong.service;

import com.mahjong.dto.MeldDTO;
import com.mahjong.model.MeldType;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import mahjongutils.models.Furo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Maps this project's tile types onto mahjong-utils tile codes (1m–9m, 1p–9p, 1s–9s, 1z–7z).
 */
final class MahjongUtilsTiles {

    private MahjongUtilsTiles() {
    }

    static mahjongutils.models.Tile toLib(TileType type) {
        // Tile.get(text) is a Kotlin value-class operator; parseTiles is the Java-callable path.
        return mahjongutils.models.Tile.Companion.parseTiles(toLibText(type)).get(0);
    }

    static List<mahjongutils.models.Tile> toLib(List<Tile> tiles) {
        List<mahjongutils.models.Tile> result = new ArrayList<>(tiles.size());
        for (Tile tile : tiles) {
            result.add(toLib(tile.getType()));
        }
        return result;
    }

    static String toLibText(TileType type) {
        return switch (type) {
            case M1 -> "1m";
            case M2 -> "2m";
            case M3 -> "3m";
            case M4 -> "4m";
            case M5 -> "5m";
            case M6 -> "6m";
            case M7 -> "7m";
            case M8 -> "8m";
            case M9 -> "9m";
            case P1 -> "1p";
            case P2 -> "2p";
            case P3 -> "3p";
            case P4 -> "4p";
            case P5 -> "5p";
            case P6 -> "6p";
            case P7 -> "7p";
            case P8 -> "8p";
            case P9 -> "9p";
            case S1 -> "1s";
            case S2 -> "2s";
            case S3 -> "3s";
            case S4 -> "4s";
            case S5 -> "5s";
            case S6 -> "6s";
            case S7 -> "7s";
            case S8 -> "8s";
            case S9 -> "9s";
            case EAST -> "1z";
            case SOUTH -> "2z";
            case WEST -> "3z";
            case NORTH -> "4z";
            case WHITE -> "5z";
            case GREEN -> "6z";
            case RED -> "7z";
        };
    }

    static Furo toFuro(MeldDTO meld) {
        if (meld == null || meld.getTiles() == null || meld.getTiles().isEmpty()) {
            return null;
        }
        List<mahjongutils.models.Tile> tiles = new ArrayList<>();
        for (TileType type : meld.getTiles()) {
            if (type != null) {
                tiles.add(toLib(type));
            }
        }
        if (tiles.size() < 3) {
            return null;
        }
        boolean ankan = meld.getType() == MeldType.KAN_CLOSED;
        try {
            return MahjongUtilsInterop.parseFuro(tiles, ankan);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static TileType fromLib(mahjongutils.models.Tile tile) {
        String text = tile.toString();
        if (text == null || text.length() != 2) {
            throw new IllegalArgumentException("Unexpected mahjong-utils tile: " + tile);
        }
        char num = text.charAt(0);
        char suit = Character.toLowerCase(text.charAt(1));
        return switch (suit) {
            case 'm' -> TileType.valueOf("M" + num);
            case 'p' -> TileType.valueOf("P" + num);
            case 's' -> TileType.valueOf("S" + num);
            case 'z' -> switch (num) {
                case '1' -> TileType.EAST;
                case '2' -> TileType.SOUTH;
                case '3' -> TileType.WEST;
                case '4' -> TileType.NORTH;
                case '5' -> TileType.WHITE;
                case '6' -> TileType.GREEN;
                case '7' -> TileType.RED;
                default -> throw new IllegalArgumentException("Unexpected honor: " + text);
            };
            default -> throw new IllegalArgumentException("Unexpected mahjong-utils tile: " + text);
        };
    }

    /**
     * Tatsu.toString() is {@code 12p} / {@code 13m} (two digits + suit).
     */
    static Set<TileType> fromTatsuText(String text) {
        if (text == null || text.length() != 3) {
            return Set.of();
        }
        char suit = text.charAt(2);
        TileType a = fromLib(mahjongutils.models.Tile.Companion.parseTiles("" + text.charAt(0) + suit).get(0));
        TileType b = fromLib(mahjongutils.models.Tile.Companion.parseTiles("" + text.charAt(1) + suit).get(0));
        return EnumSet.of(a, b);
    }
}
