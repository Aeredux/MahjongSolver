package com.mahjong.service;

import com.mahjong.dto.MeldDTO;
import com.mahjong.model.CallDecision;
import com.mahjong.model.CallType;
import com.mahjong.model.Tile;
import com.mahjong.model.TileSuit;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CallDecisionService {

    private static final Logger logger = LoggerFactory.getLogger(CallDecisionService.class);

    /**
     * Doman default: open tanyao (kuitan) is off, so chi almost never pays.
     */
    static final boolean KUITAN = false;

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Autowired
    private HandAnalyzer handAnalyzer;

    public CallDecision evaluateRon(List<Tile> hand, Tile calledTile) {
        return evaluateRon(hand, calledTile, List.of());
    }

    public CallDecision evaluateRon(List<Tile> hand, Tile calledTile, List<MeldDTO> ownMelds) {
        logger.debug("Evaluating RON decision");

        List<Tile> completeHand = new ArrayList<>(hand);
        completeHand.add(calledTile);

        boolean isWinning = shantenCalculator.isWinning(completeHand, ownMelds)
                || ((ownMelds == null || ownMelds.isEmpty()) && handAnalyzer.isWinningHand(completeHand));

        CallDecision decision = new CallDecision(CallType.RON, isWinning);
        decision.setConfidence(isWinning ? 1.0 : 0.0);
        decision.setReasoning(isWinning
                ? "Winning hand detected - call RON"
                : "Not a winning hand - do not call RON");

        return decision;
    }

    public CallDecision evaluateTsumo(List<Tile> hand) {
        return evaluateTsumo(hand, List.of());
    }

    public CallDecision evaluateTsumo(List<Tile> hand, List<MeldDTO> ownMelds) {
        logger.debug("Evaluating TSUMO decision");

        boolean isWinning = shantenCalculator.isWinning(hand, ownMelds)
                || ((ownMelds == null || ownMelds.isEmpty()) && handAnalyzer.isWinningHand(hand));

        CallDecision decision = new CallDecision(CallType.TSUMO, isWinning);
        decision.setConfidence(isWinning ? 1.0 : 0.0);
        decision.setReasoning(isWinning
                ? "Winning hand detected - call TSUMO"
                : "Not a winning hand - do not call TSUMO");

        return decision;
    }

    public CallDecision evaluateRiichi(List<Tile> hand, boolean isMenzen, int score) {
        return evaluateRiichi(hand, isMenzen, score, null, null);
    }

    public CallDecision evaluateRiichi(List<Tile> hand, boolean isMenzen, int score, Wind seatWind, Wind roundWind) {
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

        int shanten = shantenCalculator.calculateShanten(hand);
        boolean isTenpai = shanten == 0;

        if (!isTenpai) {
            return new CallDecision(CallType.RIICHI, false, 0.0,
                    String.format("Hand is not tenpai (shanten = %d) - cannot declare riichi", shanten),
                    shanten, shanten);
        }

        List<TileType> waitingTiles = new ArrayList<>(analyzeWaitsDirect(hand).advance());
        int waitCount = waitingTiles.size();
        boolean goodWait = isGoodWait(waitingTiles);
        boolean hasYaku = hasClosedYaku(hand, seatWind, roundWind);

        // RB1: riichi is not automatic. Good wait or no existing yaku → riichi;
        // damaten when the wait is poor and the hand already has yaku.
        boolean shouldCall;
        double confidence;
        String reasoning;
        if (goodWait) {
            shouldCall = true;
            confidence = Math.min(1.0, 0.75 + (waitCount * 0.08));
            reasoning = String.format(
                    "Tenpai with a good wait (%d tile type(s)). Riichi recommended.",
                    waitCount);
        } else if (!hasYaku) {
            shouldCall = true;
            confidence = 0.65;
            reasoning = String.format(
                    "Tenpai with a poor wait (%d type(s)) and no yaku without riichi — declare riichi.",
                    waitCount);
        } else {
            shouldCall = false;
            confidence = 0.35;
            reasoning = String.format(
                    "Damaten: poor wait (%d type(s)) but the hand already has yaku (tanyao/yakuhai). Skip riichi.",
                    waitCount);
        }

        return new CallDecision(CallType.RIICHI, shouldCall, confidence, reasoning, 0, 0);
    }

    public CallDecision evaluatePon(List<Tile> hand, Tile calledTile) {
        return evaluatePon(hand, calledTile, null, null);
    }

    public CallDecision evaluatePon(List<Tile> hand, Tile calledTile, Wind seatWind, Wind roundWind) {
        return evaluatePon(hand, calledTile, seatWind, roundWind, List.of(), List.of());
    }

    public CallDecision evaluatePon(
            List<Tile> hand,
            Tile calledTile,
            Wind seatWind,
            Wind roundWind,
            List<TileType> dora,
            List<MeldDTO> ownMelds
    ) {
        logger.debug("Evaluating PON decision for tile {}", calledTile.getType());

        Map<TileType, Integer> counts = DefenseHeuristics.handCounts(hand);
        int tileCount = counts.getOrDefault(calledTile.getType(), 0);

        if (tileCount < 2) {
            return new CallDecision(CallType.PON, false, 0.0,
                    "Insufficient tiles for pon (need 2 matching tiles in hand)", 0, 0);
        }

        ShantenCalculator.FuroChanceAnalysis furo =
                shantenCalculator.analyzeFuroChance(hand, calledTile.getType(), false);
        int currentShanten = furo.isAvailable()
                ? furo.getPassShanten()
                : shantenCalculator.calculateShanten(hand, ownMelds);
        int shantenAfterPon = furo.getPonShanten() != null
                ? furo.getPonShanten()
                : fallbackShantenAfterPon(hand, calledTile, ownMelds);

        boolean yakuhai = isYakuhai(calledTile.getType(), seatWind, roundWind);
        boolean doraTile = isDora(calledTile.getType(), dora);
        boolean valuable = yakuhai || doraTile;
        String valueTag = yakuhai ? " (yakuhai)" : doraTile ? " (dora)" : "";
        boolean shouldCall;
        double confidence;
        String reasoning;

        if (shantenAfterPon < currentShanten) {
            shouldCall = true;
            confidence = valuable ? 0.9 : 0.8;
            reasoning = String.format("PON improves shanten from %d to %d%s — recommended",
                    currentShanten, shantenAfterPon, valueTag);
        } else if (shantenAfterPon == currentShanten && valuable) {
            shouldCall = true;
            confidence = 0.7;
            reasoning = String.format(
                    "PON maintains shanten at %d but claims %s — call (RB1).",
                    currentShanten, yakuhai ? "yakuhai" : "dora");
        } else if (shantenAfterPon == currentShanten) {
            shouldCall = false;
            confidence = 0.25;
            reasoning = String.format(
                    "PON maintains shanten at %d — skip (do not open on a shanten tie).",
                    currentShanten);
        } else {
            shouldCall = false;
            confidence = 0.15;
            reasoning = String.format("PON worsens shanten from %d to %d — not recommended",
                    currentShanten, shantenAfterPon);
        }

        return new CallDecision(CallType.PON, shouldCall, confidence, reasoning,
                currentShanten, shantenAfterPon);
    }

    public CallDecision evaluateChi(List<Tile> hand, Tile calledTile, List<Tile> sequenceTiles) {
        return evaluateChi(hand, calledTile, sequenceTiles, null, null);
    }

    public CallDecision evaluateChi(
            List<Tile> hand,
            Tile calledTile,
            List<Tile> sequenceTiles,
            Wind seatWind,
            Wind roundWind
    ) {
        return evaluateChi(hand, calledTile, sequenceTiles, seatWind, roundWind, List.of(), List.of());
    }

    public CallDecision evaluateChi(
            List<Tile> hand,
            Tile calledTile,
            List<Tile> sequenceTiles,
            Wind seatWind,
            Wind roundWind,
            List<TileType> dora,
            List<MeldDTO> ownMelds
    ) {
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

        ShantenCalculator.FuroChanceAnalysis furo =
                shantenCalculator.analyzeFuroChance(hand, calledTile.getType(), true);
        int currentShanten = furo.isAvailable()
                ? furo.getPassShanten()
                : shantenCalculator.calculateShanten(hand, ownMelds);

        List<TileType> sequenceTypes = sequenceTiles.stream().map(Tile::getType).collect(Collectors.toList());
        Integer chiShanten = furo.chiShantenFor(sequenceTypes);
        int shantenAfterChi = chiShanten != null ? chiShanten : fallbackShantenAfterChi(hand, sequenceTiles, ownMelds);

        boolean shouldCall = false;
        double confidence = 0.15;
        String reasoning;

        if (!KUITAN && !hasYakuhaiTiles(hand, seatWind, roundWind) && !isDora(calledTile.getType(), dora)) {
            reasoning = String.format(
                    "CHI skipped: Doman / kuitan-off — almost never chi without yakuhai (pass shanten %d, chi shanten %d).",
                    currentShanten, shantenAfterChi);
        } else if (shantenAfterChi < currentShanten) {
            shouldCall = true;
            confidence = 0.65;
            reasoning = String.format("CHI improves shanten from %d to %d — call.",
                    currentShanten, shantenAfterChi);
        } else if (shantenAfterChi == currentShanten) {
            reasoning = String.format(
                    "CHI maintains shanten at %d — skip (RB1: do not chi on a shanten tie).",
                    currentShanten);
        } else {
            reasoning = String.format("CHI worsens shanten from %d to %d — not recommended",
                    currentShanten, shantenAfterChi);
        }

        return new CallDecision(CallType.CHI, shouldCall, confidence, reasoning,
                currentShanten, shantenAfterChi);
    }

    public CallDecision evaluateKan(List<Tile> hand, Tile calledTile, boolean isOpen) {
        return evaluateKan(hand, calledTile, isOpen, null, null);
    }

    public CallDecision evaluateKan(List<Tile> hand, Tile calledTile, boolean isOpen, Wind seatWind, Wind roundWind) {
        return evaluateKan(hand, calledTile, isOpen, seatWind, roundWind, List.of(), List.of());
    }

    public CallDecision evaluateKan(
            List<Tile> hand,
            Tile calledTile,
            boolean isOpen,
            Wind seatWind,
            Wind roundWind,
            List<TileType> dora,
            List<MeldDTO> ownMelds
    ) {
        logger.debug("Evaluating KAN decision for tile {}", calledTile.getType());

        Map<TileType, Integer> counts = DefenseHeuristics.handCounts(hand);
        int tileCount = counts.getOrDefault(calledTile.getType(), 0);

        if (isOpen && tileCount < 3) {
            return new CallDecision(CallType.KAN, false, 0.0,
                    "Insufficient tiles for open kan (need 3 matching tiles in hand)", 0, 0);
        }

        if (!isOpen && tileCount < 4) {
            return new CallDecision(CallType.KAN, false, 0.0,
                    "Insufficient tiles for closed kan (need 4 matching tiles in hand)", 0, 0);
        }

        ShantenCalculator.FuroChanceAnalysis furo =
                shantenCalculator.analyzeFuroChance(hand, calledTile.getType(), false);
        int currentShanten = furo.isAvailable()
                ? furo.getPassShanten()
                : shantenCalculator.calculateShanten(hand, ownMelds);
        int shantenAfterKan = furo.getMinkanShanten() != null ? furo.getMinkanShanten() : currentShanten;
        boolean yakuhai = isYakuhai(calledTile.getType(), seatWind, roundWind)
                || isDora(calledTile.getType(), dora);

        boolean shouldCall;
        double confidence;
        String reasoning;
        if (shantenAfterKan < currentShanten) {
            shouldCall = true;
            confidence = 0.75;
            reasoning = String.format("KAN improves shanten from %d to %d — recommended",
                    currentShanten, shantenAfterKan);
        } else if (currentShanten == 0 && shantenAfterKan == 0) {
            shouldCall = true;
            confidence = yakuhai ? 0.7 : 0.55;
            reasoning = "Hand is tenpai — kan is acceptable for an extra dora draw.";
        } else if (currentShanten == 1 && shantenAfterKan == 1 && yakuhai) {
            shouldCall = true;
            confidence = 0.5;
            reasoning = "1-shanten yakuhai kan — acceptable.";
        } else {
            shouldCall = false;
            confidence = 0.25;
            reasoning = String.format(
                    "KAN does not improve shanten (now %d, after %d) — skip.",
                    currentShanten, shantenAfterKan);
        }

        return new CallDecision(CallType.KAN, shouldCall, confidence, reasoning,
                currentShanten, shantenAfterKan);
    }

    private int fallbackShantenAfterPon(List<Tile> hand, Tile calledTile, List<MeldDTO> ownMelds) {
        List<Tile> handAfterPon = new ArrayList<>(hand);
        handAfterPon.remove(new Tile(calledTile.getType()));
        handAfterPon.remove(new Tile(calledTile.getType()));
        return shantenCalculator.calculateShanten(handAfterPon, ownMelds);
    }

    private int fallbackShantenAfterChi(List<Tile> hand, List<Tile> sequenceTiles, List<MeldDTO> ownMelds) {
        List<Tile> handAfterChi = new ArrayList<>(hand);
        for (Tile tile : sequenceTiles) {
            handAfterChi.remove(tile);
        }
        return shantenCalculator.calculateShanten(handAfterChi, ownMelds);
    }

    private WaitInfo analyzeWaitsDirect(List<Tile> hand) {
        try {
            var result = mahjongutils.shanten.ShantenKt.shanten(MahjongUtilsTiles.toLib(hand));
            if (result.getShantenInfo() instanceof mahjongutils.shanten.ShantenWithoutGot withoutGot
                    && withoutGot.getShantenNum() == 0) {
                return new WaitInfo(withoutGot.getAdvance().stream()
                        .map(MahjongUtilsTiles::fromLib)
                        .collect(Collectors.toSet()));
            }
        } catch (RuntimeException ignored) {
            // fall through to HandAnalyzer
        }
        return new WaitInfo(Set.copyOf(handAnalyzer.getWaitingTiles(hand)));
    }

    static boolean isGoodWait(List<TileType> waitingTiles) {
        if (waitingTiles.size() >= 2) {
            Set<TileType> waits = Set.copyOf(waitingTiles);
            for (TileType a : waits) {
                if (a.getSuit() == TileSuit.HONOR) {
                    continue;
                }
                for (TileType b : waits) {
                    if (a.getSuit() == b.getSuit() && Math.abs(a.getValue() - b.getValue()) == 3) {
                        return true; // ryanmen 1-4 / 2-5 / 3-6 ...
                    }
                    if (a.getSuit() == b.getSuit() && Math.abs(a.getValue() - b.getValue()) == 1
                            && a.getValue() >= 2 && a.getValue() <= 8 && b.getValue() >= 2 && b.getValue() <= 8) {
                        return true;
                    }
                }
            }
            return waitingTiles.size() >= 3;
        }
        return false;
    }

    static boolean isDora(TileType tile, List<TileType> dora) {
        if (tile == null || dora == null) {
            return false;
        }
        return dora.contains(tile);
    }

    static boolean isYakuhai(TileType tile, Wind seatWind, Wind roundWind) {
        if (tile.isDragon()) {
            return true;
        }
        if (seatWind != null && tile == seatWind.toTileType()) {
            return true;
        }
        return roundWind != null && tile == roundWind.toTileType();
    }

    static boolean hasYakuhaiTiles(List<Tile> hand, Wind seatWind, Wind roundWind) {
        for (Tile tile : hand) {
            if (isYakuhai(tile.getType(), seatWind, roundWind)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasClosedYaku(List<Tile> hand, Wind seatWind, Wind roundWind) {
        if (hand.stream().allMatch(t -> t.getType().isSimple())) {
            return true; // closed tanyao
        }
        Map<TileType, Integer> counts = DefenseHeuristics.handCounts(hand);
        for (Map.Entry<TileType, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 2 && isYakuhai(entry.getKey(), seatWind, roundWind)) {
                return true;
            }
        }
        return false;
    }

    private record WaitInfo(Set<TileType> advance) {
    }
}
