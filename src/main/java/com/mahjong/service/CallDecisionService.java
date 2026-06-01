package com.mahjong.service;

import com.mahjong.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CallDecisionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CallDecisionService.class);

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Autowired
    private HandAnalyzer handAnalyzer;

    public CallDecision evaluateRon(List<Tile> hand, Tile calledTile) {
        logger.debug("Evaluating RON decision");
        
        List<Tile> completeHand = new ArrayList<>(hand);
        completeHand.add(calledTile);
        
        boolean isWinning = handAnalyzer.isWinningHand(completeHand);
        
        CallDecision decision = new CallDecision(CallType.RON, isWinning);
        decision.setConfidence(isWinning ? 1.0 : 0.0);
        decision.setReasoning(isWinning ? 
            "Winning hand detected - call RON" : 
            "Not a winning hand - do not call RON");
        
        return decision;
    }

    public CallDecision evaluateTsumo(List<Tile> hand) {
        logger.debug("Evaluating TSUMO decision");
        
        boolean isWinning = handAnalyzer.isWinningHand(hand);
        
        CallDecision decision = new CallDecision(CallType.TSUMO, isWinning);
        decision.setConfidence(isWinning ? 1.0 : 0.0);
        decision.setReasoning(isWinning ? 
            "Winning hand detected - call TSUMO" : 
            "Not a winning hand - do not call TSUMO");
        
        return decision;
    }

    public CallDecision evaluateRiichi(List<Tile> hand, boolean isMenzen, int score) {
        logger.debug("Evaluating RIICHI decision");
        
        if (!isMenzen) {
            return new CallDecision(CallType.RIICHI, false, 0.0, 
                "Hand is not closed (menzen) - cannot declare riichi", 0, 0);
        }
        
        if (score < 1000) {
            return new CallDecision(CallType.RIICHI, false, 0.0, 
                "Insufficient score (need 1000 points) - cannot declare riichi", 0, 0);
        }
        
        if (hand.size() != 13) {
            return new CallDecision(CallType.RIICHI, false, 0.0, 
                "Hand size must be 13 to declare riichi", 0, 0);
        }
        
        boolean isTenpai = shantenCalculator.isTenpai(hand);
        
        if (!isTenpai) {
            int shanten = shantenCalculator.calculateShanten(hand);
            return new CallDecision(CallType.RIICHI, false, 0.0, 
                String.format("Hand is not tenpai (shanten = %d) - cannot declare riichi", shanten), 
                shanten, shanten);
        }
        
        List<TileType> waitingTiles = handAnalyzer.getWaitingTiles(hand);
        int waitCount = waitingTiles.size();
        
        double confidence = Math.min(1.0, 0.7 + (waitCount * 0.1));
        
        String reasoning = String.format(
            "Hand is tenpai with %d waiting tile type(s). Riichi recommended for bonus yaku and ura-dora.",
            waitCount
        );
        
        return new CallDecision(CallType.RIICHI, true, confidence, reasoning, 0, 0);
    }

    public CallDecision evaluatePon(List<Tile> hand, Tile calledTile) {
        logger.debug("Evaluating PON decision for tile {}", calledTile.getType());
        
        Map<TileType, Integer> counts = getTileCounts(hand);
        int tileCount = counts.getOrDefault(calledTile.getType(), 0);
        
        if (tileCount < 2) {
            return new CallDecision(CallType.PON, false, 0.0, 
                "Insufficient tiles for pon (need 2 matching tiles in hand)", 0, 0);
        }
        
        int currentShanten = shantenCalculator.calculateShanten(hand);
        
        List<Tile> handAfterPon = new ArrayList<>(hand);
        handAfterPon.remove(new Tile(calledTile.getType()));
        handAfterPon.remove(new Tile(calledTile.getType()));
        
        int shantenAfterPon = shantenCalculator.calculateShanten(handAfterPon);
        
        boolean shouldCall = shantenAfterPon <= currentShanten;
        double confidence = shouldCall ? 0.6 : 0.2;
        
        String reasoning;
        if (shantenAfterPon < currentShanten) {
            reasoning = String.format("PON improves shanten from %d to %d - recommended", 
                currentShanten, shantenAfterPon);
            confidence = 0.8;
        } else if (shantenAfterPon == currentShanten) {
            reasoning = String.format("PON maintains shanten at %d - may be acceptable for speed", 
                currentShanten);
        } else {
            reasoning = String.format("PON worsens shanten from %d to %d - not recommended", 
                currentShanten, shantenAfterPon);
            shouldCall = false;
        }
        
        CallDecision decision = new CallDecision(CallType.PON, shouldCall, confidence, reasoning, 
            currentShanten, shantenAfterPon);
        
        return decision;
    }

    public CallDecision evaluateChi(List<Tile> hand, Tile calledTile, List<Tile> sequenceTiles) {
        logger.debug("Evaluating CHI decision for tile {}", calledTile.getType());
        
        if (calledTile.getSuit() == TileSuit.HONOR) {
            return new CallDecision(CallType.CHI, false, 0.0, 
                "Cannot chi honor tiles", 0, 0);
        }
        
        if (sequenceTiles == null || sequenceTiles.size() != 2) {
            return new CallDecision(CallType.CHI, false, 0.0, 
                "Must provide exactly 2 tiles to form sequence with called tile", 0, 0);
        }
        
        List<Tile> testSequence = new ArrayList<>(sequenceTiles);
        testSequence.add(calledTile);
        
        if (!handAnalyzer.isSequence(testSequence)) {
            return new CallDecision(CallType.CHI, false, 0.0, 
                "Provided tiles do not form a valid sequence", 0, 0);
        }
        
        int currentShanten = shantenCalculator.calculateShanten(hand);
        
        List<Tile> handAfterChi = new ArrayList<>(hand);
        for (Tile tile : sequenceTiles) {
            handAfterChi.remove(tile);
        }
        
        int shantenAfterChi = shantenCalculator.calculateShanten(handAfterChi);
        
        boolean shouldCall = shantenAfterChi <= currentShanten;
        double confidence = shouldCall ? 0.5 : 0.2;
        
        String reasoning;
        if (shantenAfterChi < currentShanten) {
            reasoning = String.format("CHI improves shanten from %d to %d - recommended", 
                currentShanten, shantenAfterChi);
            confidence = 0.7;
        } else if (shantenAfterChi == currentShanten) {
            reasoning = String.format("CHI maintains shanten at %d - acceptable for speed but loses menzen", 
                currentShanten);
        } else {
            reasoning = String.format("CHI worsens shanten from %d to %d - not recommended", 
                currentShanten, shantenAfterChi);
            shouldCall = false;
        }
        
        CallDecision decision = new CallDecision(CallType.CHI, shouldCall, confidence, reasoning, 
            currentShanten, shantenAfterChi);
        
        return decision;
    }

    public CallDecision evaluateKan(List<Tile> hand, Tile calledTile, boolean isOpen) {
        logger.debug("Evaluating KAN decision for tile {}", calledTile.getType());
        
        Map<TileType, Integer> counts = getTileCounts(hand);
        int tileCount = counts.getOrDefault(calledTile.getType(), 0);
        
        if (isOpen && tileCount < 3) {
            return new CallDecision(CallType.KAN, false, 0.0, 
                "Insufficient tiles for open kan (need 3 matching tiles in hand)", 0, 0);
        }
        
        if (!isOpen && tileCount < 4) {
            return new CallDecision(CallType.KAN, false, 0.0, 
                "Insufficient tiles for closed kan (need 4 matching tiles in hand)", 0, 0);
        }
        
        int currentShanten = shantenCalculator.calculateShanten(hand);
        
        boolean shouldCall = currentShanten <= 1;
        double confidence = shouldCall ? 0.6 : 0.3;
        
        String reasoning;
        if (currentShanten == 0) {
            reasoning = "Hand is tenpai - kan recommended for additional dora";
            confidence = 0.8;
        } else if (currentShanten == 1) {
            reasoning = "Hand is 1-shanten - kan may be acceptable for dora";
            confidence = 0.6;
        } else {
            reasoning = String.format("Hand is %d-shanten - kan not recommended (focus on improving hand)", 
                currentShanten);
            shouldCall = false;
        }
        
        CallDecision decision = new CallDecision(CallType.KAN, shouldCall, confidence, reasoning, 
            currentShanten, currentShanten);
        
        return decision;
    }

    private Map<TileType, Integer> getTileCounts(List<Tile> tiles) {
        Map<TileType, Integer> counts = new HashMap<>();
        for (Tile tile : tiles) {
            counts.merge(tile.getType(), 1, Integer::sum);
        }
        return counts;
    }
}
