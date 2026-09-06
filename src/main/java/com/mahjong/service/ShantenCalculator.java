package com.mahjong.service;

import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import mahjongutils.models.Tatsu;
import mahjongutils.shanten.CommonShanten;
import mahjongutils.shanten.FuroChanceShantenResult;
import mahjongutils.shanten.ShantenKt;
import mahjongutils.shanten.ShantenWithFuroChance;
import mahjongutils.shanten.ShantenWithGot;
import mahjongutils.shanten.ShantenWithoutGot;
import mahjongutils.shanten.UnionShantenResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shanten / ukeire / furo analysis backed by mahjong-utils.
 */
@Service
public class ShantenCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ShantenCalculator.class);
    static final int MAX_SHANTEN = 8;

    public int calculateShanten(List<Tile> hand) {
        if (hand == null || hand.isEmpty()) {
            return MAX_SHANTEN;
        }
        try {
            UnionShantenResult result = ShantenKt.shanten(MahjongUtilsTiles.toLib(hand));
            return result.getShantenInfo().getShantenNum();
        } catch (RuntimeException e) {
            logger.warn("mahjong-utils rejected hand of size {}: {}", hand.size(), e.getMessage());
            return MAX_SHANTEN;
        }
    }

    public int calculateShantenAfterDiscard(List<Tile> hand, TileType discardType) {
        List<Tile> remaining = removeFirst(hand, discardType);
        if (remaining.size() == hand.size()) {
            logger.warn("Attempted to discard tile {} not in hand", discardType);
            return MAX_SHANTEN;
        }
        return calculateShanten(remaining);
    }

    public boolean isTenpai(List<Tile> hand) {
        return calculateShanten(hand) == 0;
    }

    public boolean isWinning(List<Tile> hand) {
        return calculateShanten(hand) == -1;
    }

    /**
     * Analyze every unique discard. For 3n+2 hands this is a single mahjong-utils call
     * ({@code ShantenWithGot.discardToAdvance}); otherwise each candidate is re-evaluated.
     */
    public Map<TileType, DiscardAnalysis> analyzeDiscards(List<Tile> hand) {
        Map<TileType, DiscardAnalysis> analyses = new LinkedHashMap<>();
        if (hand == null || hand.isEmpty()) {
            return analyses;
        }

        if (hand.size() % 3 == 2) {
            try {
                UnionShantenResult result = ShantenKt.shanten(MahjongUtilsTiles.toLib(hand));
                CommonShanten info = result.getShantenInfo();
                if (info instanceof ShantenWithGot withGot) {
                    for (Map.Entry<mahjongutils.models.Tile, ShantenWithoutGot> entry :
                            withGot.getDiscardToAdvance().entrySet()) {
                        TileType discard = MahjongUtilsTiles.fromLib(entry.getKey());
                        analyses.put(discard, toAnalysis(discard, entry.getValue()));
                    }
                    return analyses;
                }
            } catch (RuntimeException e) {
                logger.warn("mahjong-utils discard analysis failed: {}", e.getMessage());
            }
        }

        Set<TileType> unique = EnumSet.noneOf(TileType.class);
        for (Tile tile : hand) {
            unique.add(tile.getType());
        }
        for (TileType discard : unique) {
            analyses.put(discard, analyzeAfterDiscard(hand, discard));
        }
        return analyses;
    }

    public DiscardAnalysis analyzeAfterDiscard(List<Tile> hand, TileType discardType) {
        List<Tile> remaining = removeFirst(hand, discardType);
        if (remaining.size() == hand.size()) {
            return new DiscardAnalysis(discardType, MAX_SHANTEN, Set.of(), Set.of());
        }
        try {
            UnionShantenResult result = ShantenKt.shanten(MahjongUtilsTiles.toLib(remaining));
            CommonShanten info = result.getShantenInfo();
            if (info instanceof ShantenWithoutGot withoutGot) {
                return toAnalysis(discardType, withoutGot);
            }
            return new DiscardAnalysis(discardType, info.getShantenNum(), Set.of(), Set.of());
        } catch (RuntimeException e) {
            logger.warn("mahjong-utils analysis after discarding {} failed: {}", discardType, e.getMessage());
            return new DiscardAnalysis(discardType, MAX_SHANTEN, Set.of(), Set.of());
        }
    }

    public FuroChanceAnalysis analyzeFuroChance(List<Tile> hand, TileType chanceTile, boolean allowChi) {
        if (hand == null || hand.isEmpty() || chanceTile == null) {
            return FuroChanceAnalysis.unavailable();
        }
        try {
            FuroChanceShantenResult result = MahjongUtilsInterop.furoChanceShanten(
                    MahjongUtilsTiles.toLib(hand),
                    MahjongUtilsTiles.toLib(chanceTile),
                    allowChi
            );
            ShantenWithFuroChance info = result.getShantenInfo();
            int passShanten = info.getPass() != null
                    ? info.getPass().getShantenNum()
                    : info.getShantenNum();
            Integer ponShanten = info.getPon() != null ? info.getPon().getShantenNum() : null;
            Integer minkanShanten = info.getMinkan() != null ? info.getMinkan().getShantenNum() : null;

            Map<Set<TileType>, Integer> chiShantenByTiles = new LinkedHashMap<>();
            if (info.getChi() != null) {
                for (Map.Entry<Tatsu, ShantenWithGot> entry : info.getChi().entrySet()) {
                    Set<TileType> tiles = MahjongUtilsTiles.fromTatsuText(entry.getKey().toString());
                    if (tiles.size() == 2) {
                        chiShantenByTiles.put(tiles, entry.getValue().getShantenNum());
                    }
                }
            }
            return new FuroChanceAnalysis(
                    true,
                    info.getCanRon(),
                    passShanten,
                    ponShanten,
                    minkanShanten,
                    chiShantenByTiles
            );
        } catch (RuntimeException e) {
            logger.debug("furoChanceShanten unavailable for {}: {}", chanceTile, e.getMessage());
            return FuroChanceAnalysis.unavailable();
        }
    }

    private static DiscardAnalysis toAnalysis(TileType discard, ShantenWithoutGot after) {
        return new DiscardAnalysis(
                discard,
                after.getShantenNum(),
                toTileTypes(after.getAdvance()),
                toTileTypes(after.getGoodShapeAdvance())
        );
    }

    private static Set<TileType> toTileTypes(Set<mahjongutils.models.Tile> tiles) {
        if (tiles == null || tiles.isEmpty()) {
            return Set.of();
        }
        EnumSet<TileType> result = EnumSet.noneOf(TileType.class);
        for (mahjongutils.models.Tile tile : tiles) {
            result.add(MahjongUtilsTiles.fromLib(tile));
        }
        return result;
    }

    private static List<Tile> removeFirst(List<Tile> hand, TileType discardType) {
        List<Tile> remaining = new ArrayList<>(hand);
        for (int i = 0; i < remaining.size(); i++) {
            if (remaining.get(i).getType() == discardType) {
                remaining.remove(i);
                break;
            }
        }
        return remaining;
    }

    public static final class FuroChanceAnalysis {
        private final boolean available;
        private final boolean canRon;
        private final int passShanten;
        private final Integer ponShanten;
        private final Integer minkanShanten;
        private final Map<Set<TileType>, Integer> chiShantenByTiles;

        FuroChanceAnalysis(boolean available, boolean canRon, int passShanten, Integer ponShanten,
                           Integer minkanShanten, Map<Set<TileType>, Integer> chiShantenByTiles) {
            this.available = available;
            this.canRon = canRon;
            this.passShanten = passShanten;
            this.ponShanten = ponShanten;
            this.minkanShanten = minkanShanten;
            this.chiShantenByTiles = chiShantenByTiles;
        }

        static FuroChanceAnalysis unavailable() {
            return new FuroChanceAnalysis(false, false, MAX_SHANTEN, null, null, Map.of());
        }

        public boolean isAvailable() {
            return available;
        }

        public boolean isCanRon() {
            return canRon;
        }

        public int getPassShanten() {
            return passShanten;
        }

        public Integer getPonShanten() {
            return ponShanten;
        }

        public Integer getMinkanShanten() {
            return minkanShanten;
        }

        public Integer chiShantenFor(List<TileType> sequenceTiles) {
            if (sequenceTiles == null || sequenceTiles.size() != 2) {
                return null;
            }
            Set<TileType> key = EnumSet.of(sequenceTiles.get(0), sequenceTiles.get(1));
            return chiShantenByTiles.get(key);
        }
    }
}
