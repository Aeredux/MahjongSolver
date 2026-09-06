package com.mahjong.service;

import com.mahjong.dto.DiscardedTileDTO;
import com.mahjong.dto.MeldDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.Tile;
import com.mahjong.model.TileSuit;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Published riichi defense (genbutsu / suji / kabe / one-chance).
 *
 * <p>Defense is half-blind until KAN-54: Helper does not yet send dora, aka, own pond,
 * real tsumogiri, or our own melds.
 */
final class DefenseHeuristics {

    enum Safety {
        GENBUTSU,
        ONE_CHANCE,
        KABE,
        SUJI,
        YAOCHU,
        UNKNOWN
    }

    record OpponentRead(
            Wind wind,
            boolean riichi,
            int tsumogiriStreak,
            Set<TileType> pond,
            Set<TileType> sujiTiles
    ) {
    }

    record DefenseContext(
            List<OpponentRead> opponents,
            Map<TileType, Integer> visibleCounts,
            Map<TileType, Integer> wallRemaining
    ) {
    }

    record TileDefense(
            int dangerScore,
            List<String> notes
    ) {
    }

    private DefenseHeuristics() {
    }

    static DefenseContext build(
            List<Tile> hand,
            List<PlayerDiscardsDTO> opponents,
            List<TileType> ownDiscards
    ) {
        List<PlayerDiscardsDTO> safeOpponents = opponents != null ? opponents : List.of();
        List<TileType> safeOwn = ownDiscards != null ? ownDiscards : List.of();

        Map<TileType, Integer> visible = new EnumMap<>(TileType.class);
        for (Tile tile : hand) {
            visible.merge(tile.getType(), 1, Integer::sum);
        }
        for (TileType own : safeOwn) {
            if (own != null) {
                visible.merge(own, 1, Integer::sum);
            }
        }

        List<OpponentRead> reads = new ArrayList<>();
        for (PlayerDiscardsDTO opponent : safeOpponents) {
            Set<TileType> pond = EnumSet.noneOf(TileType.class);
            int streak = 0;
            boolean countingStreak = true;
            if (opponent.getDiscards() != null) {
                List<DiscardedTileDTO> discards = opponent.getDiscards();
                for (int i = discards.size() - 1; i >= 0; i--) {
                    DiscardedTileDTO discarded = discards.get(i);
                    if (discarded.getTile() == null) {
                        continue;
                    }
                    pond.add(discarded.getTile());
                    visible.merge(discarded.getTile(), 1, Integer::sum);
                    if (countingStreak) {
                        if (discarded.isTsumogiri()) {
                            streak++;
                        } else {
                            countingStreak = false;
                        }
                    }
                }
            }
            if (opponent.getMelds() != null) {
                for (MeldDTO meld : opponent.getMelds()) {
                    if (meld.getTiles() == null) {
                        continue;
                    }
                    for (TileType tile : meld.getTiles()) {
                        if (tile != null) {
                            visible.merge(tile, 1, Integer::sum);
                        }
                    }
                }
            }
            reads.add(new OpponentRead(
                    opponent.getWind(),
                    opponent.isRiichi(),
                    streak,
                    pond,
                    sujiFromPond(pond)
            ));
        }

        Map<TileType, Integer> remaining = new EnumMap<>(TileType.class);
        for (TileType type : TileType.values()) {
            remaining.put(type, Math.max(0, 4 - visible.getOrDefault(type, 0)));
        }
        return new DefenseContext(reads, visible, remaining);
    }

    static TileDefense evaluate(TileType tile, DefenseContext context) {
        List<String> notes = new ArrayList<>();
        if (context.opponents.isEmpty()) {
            int danger = tile.isTerminalOrHonor() ? 1 : 3;
            if (tile.isTerminalOrHonor()) {
                notes.add("Terminal/honor tile — generally safer to discard.");
            }
            return new TileDefense(danger, notes);
        }

        int danger = 0;
        List<String> genbutsuAgainst = new ArrayList<>();
        List<String> sujiAgainst = new ArrayList<>();
        List<String> oneChanceAgainst = new ArrayList<>();
        List<String> kabeAgainst = new ArrayList<>();
        List<String> caution = new ArrayList<>();

        for (OpponentRead opponent : context.opponents) {
            int threat = threatWeight(opponent);
            Safety safety = classify(tile, opponent, context.visibleCounts);
            danger += dangerPoints(safety, threat);

            String who = opponent.wind != null ? opponent.wind.name() : "unknown";
            String riichiTag = opponent.riichi ? " (riichi)" : "";
            switch (safety) {
                case GENBUTSU -> genbutsuAgainst.add(who + riichiTag);
                case ONE_CHANCE -> oneChanceAgainst.add(who + riichiTag);
                case KABE -> kabeAgainst.add(who + riichiTag);
                case SUJI -> sujiAgainst.add(who + riichiTag);
                case UNKNOWN, YAOCHU -> {
                    if (opponent.riichi || opponent.tsumogiriStreak >= 2) {
                        caution.add(who + (opponent.riichi
                                ? " (riichi)"
                                : " (possible tenpai: " + opponent.tsumogiriStreak + " tsumogiri)"));
                    }
                }
            }
        }

        if (!genbutsuAgainst.isEmpty()) {
            notes.add("Genbutsu safe vs " + String.join(", ", genbutsuAgainst) + ".");
        }
        if (!oneChanceAgainst.isEmpty()) {
            notes.add("One-chance vs " + String.join(", ", oneChanceAgainst) + ".");
        }
        if (!kabeAgainst.isEmpty()) {
            notes.add("Kabe-safe vs " + String.join(", ", kabeAgainst) + ".");
        }
        if (!sujiAgainst.isEmpty()) {
            notes.add("Suji vs " + String.join(", ", sujiAgainst) + ".");
        }
        if (!caution.isEmpty()) {
            notes.add("Caution: " + String.join(", ", caution) + ".");
        }
        if (tile.isTerminalOrHonor()) {
            notes.add("Terminal/honor tile — generally safer to discard.");
        }
        return new TileDefense(danger, notes);
    }

    static int countRemaining(TileType type, Map<TileType, Integer> wallRemaining) {
        return wallRemaining.getOrDefault(type, 0);
    }

    static int ukeireCount(Set<TileType> advance, Map<TileType, Integer> wallRemaining) {
        int total = 0;
        for (TileType type : advance) {
            total += countRemaining(type, wallRemaining);
        }
        return total;
    }

    private static Safety classify(TileType tile, OpponentRead opponent, Map<TileType, Integer> visible) {
        if (opponent.pond.contains(tile)) {
            return Safety.GENBUTSU;
        }
        if (isOneChance(tile, visible)) {
            return Safety.ONE_CHANCE;
        }
        if (isKabeSafe(tile, visible)) {
            return Safety.KABE;
        }
        if (opponent.sujiTiles.contains(tile)) {
            return Safety.SUJI;
        }
        if (tile.isTerminalOrHonor()) {
            return Safety.YAOCHU;
        }
        return Safety.UNKNOWN;
    }

    private static int threatWeight(OpponentRead opponent) {
        if (opponent.riichi) {
            return 4;
        }
        if (opponent.tsumogiriStreak >= 4) {
            return 3;
        }
        if (opponent.tsumogiriStreak >= 2) {
            return 2;
        }
        return 1;
    }

    private static int dangerPoints(Safety safety, int threat) {
        int base = switch (safety) {
            case GENBUTSU -> 0;
            case ONE_CHANCE -> 1;
            case KABE -> 2;
            case SUJI -> 3;
            case YAOCHU -> 4;
            case UNKNOWN -> 6;
        };
        return base * threat;
    }

    /**
     * A discard is one-chance when three copies of that tile (or of the middle of its suji)
     * are already visible, so only one deal-in copy remains.
     */
    static boolean isOneChance(TileType tile, Map<TileType, Integer> visible) {
        if (visible.getOrDefault(tile, 0) >= 3) {
            return true;
        }
        if (tile.getSuit() == TileSuit.HONOR) {
            return false;
        }
        for (int mid : sujiMids(tile.getValue())) {
            TileType midTile = suited(tile.getSuit(), mid);
            if (midTile != null && visible.getOrDefault(midTile, 0) >= 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kabe: all four copies of a neighbouring number are out, so ryanmen / kanchan
     * through that number is dead.
     */
    static boolean isKabeSafe(TileType tile, Map<TileType, Integer> visible) {
        if (tile.getSuit() == TileSuit.HONOR) {
            return false;
        }
        int value = tile.getValue();
        int[] neighbours = {value - 1, value + 1};
        for (int neighbour : neighbours) {
            TileType wall = suited(tile.getSuit(), neighbour);
            if (wall != null && visible.getOrDefault(wall, 0) >= 4) {
                return true;
            }
        }
        // Strong kabe: all four 4s kill 1-4 ryanmen; all four 6s kill 6-9.
        if (value == 1) {
            TileType four = suited(tile.getSuit(), 4);
            return four != null && visible.getOrDefault(four, 0) >= 4;
        }
        if (value == 9) {
            TileType six = suited(tile.getSuit(), 6);
            return six != null && visible.getOrDefault(six, 0) >= 4;
        }
        return false;
    }

    static Set<TileType> sujiFromPond(Set<TileType> pond) {
        Set<TileType> suji = EnumSet.noneOf(TileType.class);
        for (TileType discarded : pond) {
            if (discarded.getSuit() == TileSuit.HONOR) {
                continue;
            }
            for (int partner : sujiPartners(discarded.getValue())) {
                TileType tile = suited(discarded.getSuit(), partner);
                if (tile != null) {
                    suji.add(tile);
                }
            }
        }
        return suji;
    }

    static int[] sujiPartners(int value) {
        return switch (value) {
            case 1 -> new int[]{4};
            case 2 -> new int[]{5};
            case 3 -> new int[]{6};
            case 4 -> new int[]{1, 7};
            case 5 -> new int[]{2, 8};
            case 6 -> new int[]{3, 9};
            case 7 -> new int[]{4};
            case 8 -> new int[]{5};
            case 9 -> new int[]{6};
            default -> new int[0];
        };
    }

    private static int[] sujiMids(int value) {
        return switch (value) {
            case 1, 7 -> new int[]{4};
            case 2, 8 -> new int[]{5};
            case 3, 9 -> new int[]{6};
            case 4 -> new int[]{1, 7};
            case 5 -> new int[]{2, 8};
            case 6 -> new int[]{3, 9};
            default -> new int[0];
        };
    }

    private static TileType suited(TileSuit suit, int value) {
        if (value < 1 || value > 9) {
            return null;
        }
        String prefix = switch (suit) {
            case MANZU -> "M";
            case PINZU -> "P";
            case SOUZU -> "S";
            default -> null;
        };
        if (prefix == null) {
            return null;
        }
        return TileType.valueOf(prefix + value);
    }

    static Map<TileType, Integer> handCounts(List<Tile> hand) {
        Map<TileType, Integer> counts = new HashMap<>();
        for (Tile tile : hand) {
            counts.merge(tile.getType(), 1, Integer::sum);
        }
        return counts;
    }
}
