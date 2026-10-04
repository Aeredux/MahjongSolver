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
    void honbaZeroWhenKnownDoesNotFold() {
        TableSituation table = TableSituation.from(
                0, 0, 1, 25000, 25000, 25000, 25000, List.of(), null, Wind.EAST);
        assertTrue(table.present(), "honba 0 is a known value, not unread");
        assertEquals(0, table.honba());
        assertEquals(0, table.riichiSticks());
        assertEquals(0, table.foldPressure());
        assertFalse(table.preferDefense());
    }

    @Test
    void honbaThreeCrossesFoldThreshold() {
        TableSituation table = TableSituation.from(
                3, 0, 1, null, null, null, null, List.of(), null, Wind.EAST);
        assertTrue(table.present());
        assertEquals(6, table.foldPressure());
        assertTrue(table.preferDefense());
        assertTrue(table.cautiousRiichi());
        assertTrue(table.skipNonImprovingCalls());
    }

    @Test
    void southFourFirstPlaceThinLeadFolds() {
        TableSituation table = TableSituation.from(
                0, 0, 4, 32000, 30000, 25000, 23000, List.of(), null, Wind.SOUTH);
        assertTrue(table.isFirstPlace());
        assertTrue(table.isLateRound());
        assertEquals(2000, table.leadOverClosest());
        assertTrue(table.preferDefense());
    }

    @Test
    void eastFourThinLeadDoesNotCountAsOras() {
        TableSituation table = TableSituation.from(
                0, 0, 4, 32000, 30000, 25000, 23000, List.of(), null, Wind.EAST);
        assertTrue(table.isFirstPlace());
        assertFalse(table.isLateRound(), "round_number is kyoku within wind; East 4 is not all last");
        assertFalse(table.preferDefense());
    }

    @Test
    void lastPlaceFarBehindPushesThroughHonba() {
        TableSituation table = TableSituation.from(
                3, 0, 4, 8000, 25000, 28000, 30000, List.of(), null, Wind.SOUTH);
        assertTrue(table.isLastPlace());
        assertTrue(table.trailBehindThird() >= 12000);
        assertFalse(table.preferDefense(), "Last and far behind should still push (pressure drops below fold)");
    }

    @Test
    void singleOpponentRiichiDoesNotCrossFoldThreshold() {
        PlayerDiscardsDTO riichi = new PlayerDiscardsDTO();
        riichi.setRiichi(true);
        TableSituation table = TableSituation.from(
                0, 0, 1, 25000, 25000, 25000, 25000, List.of(riichi), null, Wind.EAST);
        assertTrue(table.opponentRiichi());
        assertEquals(3, table.foldPressure());
        assertFalse(table.preferDefense());
        assertFalse(table.cautiousRiichi());
        assertFalse(table.skipNonImprovingCalls());
        assertTrue(table.rankDangerBeforeOffense());
    }

    @Test
    void opponentRiichiAloneIsNotDropped() {
        PlayerDiscardsDTO riichi = new PlayerDiscardsDTO();
        riichi.setRiichi(true);
        TableSituation table = TableSituation.from(
                null, null, null, null, null, null, null, List.of(riichi), null, null);
        assertTrue(table.present());
        assertTrue(table.opponentRiichi());
        assertEquals(3, table.foldPressure());
        assertFalse(table.preferDefense());
        assertTrue(table.rankDangerBeforeOffense());
    }

    @Test
    void lastPlaceWithOpponentRiichiStillPushesCalls() {
        PlayerDiscardsDTO riichi = new PlayerDiscardsDTO();
        riichi.setRiichi(true);
        TableSituation table = TableSituation.from(
                0, 0, 1, 8000, 25000, 28000, 30000, List.of(riichi), null, Wind.EAST);
        assertTrue(table.isLastPlace());
        assertFalse(table.preferDefense());
        assertTrue(table.rankDangerBeforeOffense());
    }

    @Test
    void alreadyRiichiPrefersDefenseWithoutHonba() {
        PlayerDiscardsDTO self = new PlayerDiscardsDTO();
        self.setRiichi(true);
        TableSituation table = TableSituation.from(
                null, null, null, null, null, null, null, List.of(), self, null);
        assertTrue(table.alreadyRiichi());
        assertTrue(table.preferDefense());
    }

    @Test
    void flatScoresAlignRightOppositeLeft() {
        TableSituation table = TableSituation.from(
                0, 0, 1, 25000, 11111, 22222, 33333, List.of(), null, Wind.EAST);
        assertEquals(11111, table.rightScore());
        assertEquals(22222, table.oppositeScore());
        assertEquals(33333, table.leftScore());
    }
}
