package com.mahjong.service;

import com.mahjong.dto.DiscardedTileDTO;
import com.mahjong.dto.MeldDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.MeldType;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void oneChanceIsNotASafetyTag() {
        List<Tile> hand = List.of(
            new Tile(TileType.P5), new Tile(TileType.S1), new Tile(TileType.EAST), new Tile(TileType.M6));
        PlayerDiscardsDTO oneChance = new PlayerDiscardsDTO(
            Wind.SOUTH,
            List.of(),
            false,
            List.of(new MeldDTO(MeldType.PON, List.of(TileType.P2, TileType.P2, TileType.P2))));
        DefenseHeuristics.DefenseContext ctx = DefenseHeuristics.build(hand, List.of(oneChance), List.of());
        assertTrue(DefenseHeuristics.isOneChance(TileType.P5, ctx.visibleCounts()));
        assertTrue(DefenseHeuristics.isUnknownSimple(TileType.P5, ctx),
            "A one-chance simple is still an untagged simple");
        DefenseHeuristics.TileDefense p5 = DefenseHeuristics.evaluate(TileType.P5, ctx);
        DefenseHeuristics.TileDefense terminal = DefenseHeuristics.evaluate(TileType.S1, ctx);
        DefenseHeuristics.TileDefense honor = DefenseHeuristics.evaluate(TileType.EAST, ctx);
        assertTrue(p5.notes().stream().anyMatch(note -> note.contains("One-chance")));
        assertTrue(p5.dangerScore() > terminal.dangerScore(),
            "One-chance must not score safer than a terminal");
        assertTrue(p5.dangerScore() > honor.dangerScore(),
            "One-chance must not score safer than an honor");

        PlayerDiscardsDTO suji = new PlayerDiscardsDTO(
            Wind.SOUTH,
            List.of(new DiscardedTileDTO(TileType.P2, false)),
            false,
            List.of(new MeldDTO(MeldType.PON, List.of(TileType.P8, TileType.P8, TileType.P8))));
        DefenseHeuristics.DefenseContext sujiCtx = DefenseHeuristics.build(hand, List.of(suji), List.of());
        assertTrue(DefenseHeuristics.isOneChance(TileType.P5, sujiCtx.visibleCounts()));
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.P5, sujiCtx),
            "Suji still protects a simple that is also one-chance");
        assertEquals(3, DefenseHeuristics.evaluate(TileType.P5, sujiCtx).dangerScore(),
            "Suji, not one-chance, is the safety class");

        PlayerDiscardsDTO kabe = new PlayerDiscardsDTO(
            Wind.SOUTH,
            List.of(),
            false,
            List.of(
                new MeldDTO(MeldType.KAN_OPEN, List.of(TileType.P4, TileType.P4, TileType.P4, TileType.P4)),
                new MeldDTO(MeldType.PON, List.of(TileType.P2, TileType.P2, TileType.P2))));
        DefenseHeuristics.DefenseContext kabeCtx = DefenseHeuristics.build(hand, List.of(kabe), List.of());
        assertTrue(DefenseHeuristics.isKabeSafe(TileType.P5, kabeCtx.visibleCounts()));
        assertTrue(DefenseHeuristics.isOneChance(TileType.P5, kabeCtx.visibleCounts()));
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.P5, kabeCtx));
        assertEquals(2, DefenseHeuristics.evaluate(TileType.P5, kabeCtx).dangerScore(),
            "Kabe, not one-chance, is the safety class");

        PlayerDiscardsDTO genbutsu = new PlayerDiscardsDTO(
            Wind.SOUTH,
            List.of(
                new DiscardedTileDTO(TileType.P5, false),
                new DiscardedTileDTO(TileType.P5, false),
                new DiscardedTileDTO(TileType.P5, false)),
            false,
            List.of());
        DefenseHeuristics.DefenseContext genbutsuCtx = DefenseHeuristics.build(hand, List.of(genbutsu), List.of());
        assertTrue(DefenseHeuristics.isOneChance(TileType.P5, genbutsuCtx.visibleCounts()));
        assertFalse(DefenseHeuristics.isUnknownSimple(TileType.P5, genbutsuCtx));
        DefenseHeuristics.TileDefense safe = DefenseHeuristics.evaluate(TileType.P5, genbutsuCtx);
        assertEquals(0, safe.dangerScore());
        assertTrue(safe.notes().stream().anyMatch(note -> note.contains("Genbutsu")));
    }
}
