package com.mahjong.service;

import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.Wind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableSituationTest {

    @Test
    void absentPayloadKeepsPushPosture() {
        assertFalse(TableSituation.NONE.preferDefense());
        assertEquals(0, TableSituation.NONE.foldPressure());
        assertFalse(TableSituation.NONE.present());
    }

    @Test
    void honbaThreeCrossesFoldThreshold() {
        TableSituation table = TableSituation.from(
                3, 0, 1, null, null, null, null, null, List.of(), null);
        assertTrue(table.present());
        assertEquals(6, table.foldPressure());
        assertTrue(table.preferDefense());
        assertTrue(table.cautiousRiichi());
        assertTrue(table.skipNonImprovingCalls());
    }

    @Test
    void evenEastOneScoresDoNotFold() {
        TableSituation table = TableSituation.from(
                0, 0, 1, 25000, 25000, 25000, 25000, null, List.of(), null);
        assertTrue(table.present());
        assertEquals(0, table.foldPressure());
        assertFalse(table.preferDefense());
    }

    @Test
    void lateFirstPlaceThinLeadFolds() {
        TableSituation table = TableSituation.from(
                0, 0, 8, 32000, 30000, 25000, 23000, null, List.of(), null);
        assertTrue(table.isFirstPlace());
        assertTrue(table.isLateRound());
        assertEquals(2000, table.leadOverClosest());
        assertTrue(table.preferDefense());
    }

    @Test
    void lastPlaceFarBehindPushesThroughHonba() {
        TableSituation table = TableSituation.from(
                3, 0, 8, 8000, 25000, 28000, 30000, null, List.of(), null);
        assertTrue(table.isLastPlace());
        assertTrue(table.trailBehindThird() >= 12000);
        assertFalse(table.preferDefense(), "Last and far behind should still push (pressure drops below fold)");
    }

    @Test
    void alreadyRiichiPrefersDefenseWithoutHonba() {
        PlayerDiscardsDTO self = new PlayerDiscardsDTO();
        self.setRiichi(true);
        TableSituation table = TableSituation.from(
                null, null, null, null, null, null, null, null, List.of(), self);
        assertTrue(table.alreadyRiichi());
        assertTrue(table.preferDefense());
    }

    @Test
    void opponentScoresAlignRightOppositeLeft() {
        PlayerDiscardsDTO right = new PlayerDiscardsDTO(Wind.SOUTH, List.of(), false, List.of());
        right.setScore(11111);
        PlayerDiscardsDTO opposite = new PlayerDiscardsDTO(Wind.WEST, List.of(), false, List.of());
        opposite.setScore(22222);
        PlayerDiscardsDTO left = new PlayerDiscardsDTO(Wind.NORTH, List.of(), false, List.of());
        left.setScore(33333);

        TableSituation fromNested = TableSituation.from(
                0, 0, 1, 25000, null, null, null, null,
                List.of(right, opposite, left), null);
        assertEquals(11111, fromNested.rightScore());
        assertEquals(22222, fromNested.oppositeScore());
        assertEquals(33333, fromNested.leftScore());

        TableSituation fromList = TableSituation.from(
                0, 0, 1, 25000, 1, 2, 3, List.of(40000, 30000, 20000),
                List.of(), null);
        assertEquals(40000, fromList.rightScore());
        assertEquals(30000, fromList.oppositeScore());
        assertEquals(20000, fromList.leftScore());
    }
}
