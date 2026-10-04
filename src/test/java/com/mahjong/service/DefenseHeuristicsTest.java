package com.mahjong.service;

import com.mahjong.dto.DiscardedTileDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefenseHeuristicsTest {

    @Test
    void sujiPartnersFollow147258369() {
        Set<TileType> pond = EnumSet.of(TileType.M4);
        Set<TileType> suji = DefenseHeuristics.sujiFromPond(pond);
        assertTrue(suji.contains(TileType.M1));
        assertTrue(suji.contains(TileType.M7));
        assertFalse(suji.contains(TileType.M2));
    }

    @Test
    void kabeWhenAllFourNeighbourTilesAreVisible() {
        Map<TileType, Integer> visible = new EnumMap<>(TileType.class);
        visible.put(TileType.P4, 4);
        assertTrue(DefenseHeuristics.isKabeSafe(TileType.P3, visible));
        assertTrue(DefenseHeuristics.isKabeSafe(TileType.P5, visible));
        assertTrue(DefenseHeuristics.isKabeSafe(TileType.P1, visible));
        assertFalse(DefenseHeuristics.isKabeSafe(TileType.P9, visible));
    }

    @Test
    void oneChanceWhenThreeCopiesVisible() {
        Map<TileType, Integer> visible = new EnumMap<>(TileType.class);
        visible.put(TileType.S5, 3);
        assertTrue(DefenseHeuristics.isOneChance(TileType.S5, visible));
        assertTrue(DefenseHeuristics.isOneChance(TileType.S2, visible));
        assertTrue(DefenseHeuristics.isOneChance(TileType.S8, visible));
        assertFalse(DefenseHeuristics.isOneChance(TileType.S1, visible));
    }

    @Test
    void genbutsuAgainstEveryPondLowersDanger() {
        List<Tile> hand = List.of(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );
        PlayerDiscardsDTO south = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(new DiscardedTileDTO(TileType.EAST, false)), false, List.of());
        DefenseHeuristics.DefenseContext ctx = DefenseHeuristics.build(hand, List.of(south), List.of());

        int eastDanger = DefenseHeuristics.evaluate(TileType.EAST, ctx).dangerScore();
        int westDanger = DefenseHeuristics.evaluate(TileType.WEST, ctx).dangerScore();
        assertTrue(eastDanger < westDanger, "Genbutsu EAST must be safer than WEST vs the same pond");
        assertTrue(DefenseHeuristics.evaluate(TileType.EAST, ctx).notes().stream()
            .anyMatch(n -> n.contains("Genbutsu")));
    }

    @Test
    void unknownSimpleIsAnUntaggedSimpleOnly() {
        List<Tile> hand = List.of(new Tile(TileType.P5), new Tile(TileType.S1), new Tile(TileType.EAST));
        DefenseHeuristics.DefenseContext noOpponents = DefenseHeuristics.build(hand, List.of(), List.of());
        assertTrue(DefenseHeuristics.isUnknownSimple(TileType.P5, noOpponents));
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.S1, noOpponents));
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.EAST, noOpponents));

        PlayerDiscardsDTO south = new PlayerDiscardsDTO(
            Wind.SOUTH,
            List.of(new DiscardedTileDTO(TileType.P7, false), new DiscardedTileDTO(TileType.M4, false)),
            false,
            List.of());
        DefenseHeuristics.DefenseContext ctx = DefenseHeuristics.build(hand, List.of(south), List.of());
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.P7, ctx), "Pond P7 is genbutsu");
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.M7, ctx), "M4 in the pond makes M7 suji");
        assertTrue(DefenseHeuristics.isUnknownSimple(TileType.P5, ctx));
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.S1, ctx));
    }
}
