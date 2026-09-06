package com.mahjong.service;

import com.mahjong.model.TileType;

import java.util.Collections;
import java.util.Set;

/**
 * Shanten / ukeire / good-shape snapshot for one discard, sourced from mahjong-utils.
 */
public final class DiscardAnalysis {
    private final TileType discard;
    private final int shanten;
    private final Set<TileType> advance;
    private final Set<TileType> goodShapeAdvance;

    public DiscardAnalysis(TileType discard, int shanten, Set<TileType> advance, Set<TileType> goodShapeAdvance) {
        this.discard = discard;
        this.shanten = shanten;
        this.advance = advance == null ? Set.of() : Set.copyOf(advance);
        this.goodShapeAdvance = goodShapeAdvance == null ? Set.of() : Set.copyOf(goodShapeAdvance);
    }

    public TileType getDiscard() {
        return discard;
    }

    public int getShanten() {
        return shanten;
    }

    public Set<TileType> getAdvance() {
        return Collections.unmodifiableSet(advance);
    }

    public Set<TileType> getGoodShapeAdvance() {
        return Collections.unmodifiableSet(goodShapeAdvance);
    }
}
