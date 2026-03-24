package com.mahjong.service;

import com.mahjong.model.CallDecision;
import com.mahjong.model.CallType;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CallDecisionServiceTest {

    @Autowired
    private CallDecisionService callDecisionService;

    @Test
    void testEvaluateRonWithWinningHand() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );
        Tile calledTile = new Tile(TileType.S5);

        CallDecision decision = callDecisionService.evaluateRon(hand, calledTile);

        assertEquals(CallType.RON, decision.getCallType());
        assertTrue(decision.isShouldCall());
        assertEquals(1.0, decision.getConfidence());
    }

    @Test
    void testEvaluateRonWithNonWinningHand() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M3), new Tile(TileType.M5),
            new Tile(TileType.P2), new Tile(TileType.P4), new Tile(TileType.P6),
            new Tile(TileType.S1), new Tile(TileType.S3), new Tile(TileType.S5),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH), new Tile(TileType.WEST),
            new Tile(TileType.WHITE)
        );
        Tile calledTile = new Tile(TileType.NORTH);

        CallDecision decision = callDecisionService.evaluateRon(hand, calledTile);

        assertEquals(CallType.RON, decision.getCallType());
        assertFalse(decision.isShouldCall());
        assertEquals(0.0, decision.getConfidence());
    }

    @Test
    void testEvaluateTsumoWithWinningHand() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S5)
        );

        CallDecision decision = callDecisionService.evaluateTsumo(hand);

        assertEquals(CallType.TSUMO, decision.getCallType());
        assertTrue(decision.isShouldCall());
        assertEquals(1.0, decision.getConfidence());
    }

    @Test
    void testEvaluateRiichiWithTenpaiHand() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );

        CallDecision decision = callDecisionService.evaluateRiichi(tenpaiHand, true, 25000);

        assertEquals(CallType.RIICHI, decision.getCallType());
        assertTrue(decision.isShouldCall());
        assertTrue(decision.getConfidence() > 0.5);
    }

    @Test
    void testEvaluateRiichiWithNonTenpaiHand() {
        List<Tile> nonTenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M3), new Tile(TileType.M5),
            new Tile(TileType.P2), new Tile(TileType.P4), new Tile(TileType.P6),
            new Tile(TileType.S1), new Tile(TileType.S3), new Tile(TileType.S5),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH), new Tile(TileType.WEST),
            new Tile(TileType.WHITE)
        );

        CallDecision decision = callDecisionService.evaluateRiichi(nonTenpaiHand, true, 25000);

        assertEquals(CallType.RIICHI, decision.getCallType());
        assertFalse(decision.isShouldCall());
    }

    @Test
    void testEvaluateRiichiWithOpenHand() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );

        CallDecision decision = callDecisionService.evaluateRiichi(hand, false, 25000);

        assertEquals(CallType.RIICHI, decision.getCallType());
        assertFalse(decision.isShouldCall());
        assertTrue(decision.getReasoning().contains("not closed"));
    }

    @Test
    void testEvaluateRiichiWithInsufficientScore() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5)
        );

        CallDecision decision = callDecisionService.evaluateRiichi(tenpaiHand, true, 500);

        assertEquals(CallType.RIICHI, decision.getCallType());
        assertFalse(decision.isShouldCall());
        assertTrue(decision.getReasoning().contains("Insufficient score"));
    }

    @Test
    void testEvaluatePonWithSufficientTiles() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P5), new Tile(TileType.P5),
            new Tile(TileType.S1), new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH)
        );
        Tile calledTile = new Tile(TileType.P5);

        CallDecision decision = callDecisionService.evaluatePon(hand, calledTile);

        assertEquals(CallType.PON, decision.getCallType());
        assertNotNull(decision.getReasoning());
    }

    @Test
    void testEvaluatePonWithInsufficientTiles() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P5),
            new Tile(TileType.S1), new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH), new Tile(TileType.WEST)
        );
        Tile calledTile = new Tile(TileType.P5);

        CallDecision decision = callDecisionService.evaluatePon(hand, calledTile);

        assertEquals(CallType.PON, decision.getCallType());
        assertFalse(decision.isShouldCall());
        assertTrue(decision.getReasoning().contains("Insufficient tiles"));
    }

    @Test
    void testEvaluateChiWithValidSequence() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5),
            new Tile(TileType.P1), new Tile(TileType.P2),
            new Tile(TileType.S1), new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.EAST), new Tile(TileType.SOUTH), new Tile(TileType.WEST)
        );
        Tile calledTile = new Tile(TileType.P3);
        List<Tile> sequenceTiles = Arrays.asList(
            new Tile(TileType.P1),
            new Tile(TileType.P2)
        );

        CallDecision decision = callDecisionService.evaluateChi(hand, calledTile, sequenceTiles);

        assertEquals(CallType.CHI, decision.getCallType());
        assertNotNull(decision.getReasoning());
    }

    @Test
    void testEvaluateChiWithHonorTile() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3)
        );
        Tile calledTile = new Tile(TileType.EAST);
        List<Tile> sequenceTiles = Arrays.asList(
            new Tile(TileType.SOUTH),
            new Tile(TileType.WEST)
        );

        CallDecision decision = callDecisionService.evaluateChi(hand, calledTile, sequenceTiles);

        assertEquals(CallType.CHI, decision.getCallType());
        assertFalse(decision.isShouldCall());
        assertTrue(decision.getReasoning().contains("Cannot chi honor tiles"));
    }

    @Test
    void testEvaluateKanWithSufficientTiles() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P5), new Tile(TileType.P5), new Tile(TileType.P5),
            new Tile(TileType.S5)
        );
        Tile calledTile = new Tile(TileType.P5);

        CallDecision decision = callDecisionService.evaluateKan(hand, calledTile, true);

        assertEquals(CallType.KAN, decision.getCallType());
        assertNotNull(decision.getReasoning());
    }

    @Test
    void testEvaluateKanWithInsufficientTiles() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.P5), new Tile(TileType.P5)
        );
        Tile calledTile = new Tile(TileType.P5);

        CallDecision decision = callDecisionService.evaluateKan(hand, calledTile, true);

        assertEquals(CallType.KAN, decision.getCallType());
        assertFalse(decision.isShouldCall());
        assertTrue(decision.getReasoning().contains("Insufficient tiles"));
    }
}
