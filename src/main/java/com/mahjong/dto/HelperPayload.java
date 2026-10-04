package com.mahjong.dto;

import com.mahjong.model.TileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves overlapping Helper fields so suggest-move / evaluate-call can read
 * top-level {@code dora}/{@code melds}/{@code discard_tiles} or the nested {@code player} object.
 */
public final class HelperPayload {

    private HelperPayload() {
    }

    public static List<TileType> dora(List<TileType> dora) {
        return dora != null ? dora : List.of();
    }

    public static List<MeldDTO> ownMelds(List<MeldDTO> melds, PlayerDiscardsDTO player) {
        if (melds != null && !melds.isEmpty()) {
            return melds;
        }
        if (player != null && player.getMelds() != null && !player.getMelds().isEmpty()) {
            return player.getMelds();
        }
        return List.of();
    }

    public static List<TileType> ownDiscards(List<TileType> discardTiles, PlayerDiscardsDTO player) {
        if (discardTiles != null && !discardTiles.isEmpty()) {
            return discardTiles;
        }
        if (player == null || player.getDiscards() == null || player.getDiscards().isEmpty()) {
            return discardTiles != null ? discardTiles : List.of();
        }
        List<TileType> fromPlayer = new ArrayList<>();
        for (DiscardedTileDTO discarded : player.getDiscards()) {
            if (discarded != null && discarded.getTile() != null) {
                fromPlayer.add(discarded.getTile());
            }
        }
        return fromPlayer;
    }
}
