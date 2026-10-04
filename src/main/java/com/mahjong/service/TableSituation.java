package com.mahjong.service;

import com.mahjong.dto.CallDecisionRequest;
import com.mahjong.dto.HandRequest;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.Wind;

import java.util.ArrayList;
import java.util.List;

/**
 * Honba / riichi sticks / kyoku / flat table scores for push-vs-fold.
 * Locked to Helper KAN-91 ({@code 9a6ee88}): {@code honba}, {@code riichi_sticks},
 * {@code round_number} (kyoku within {@code round_wind}), {@code player_score},
 * {@code right_score}/{@code opposite_score}/{@code left_score} aligned with
 * {@code opponents[0..2]}. Omitted keys are unread; {@code 0} is valid when present.
 */
public final class TableSituation {

    public static final TableSituation NONE = new TableSituation(
            0, 0, 0, null, null, null, null, null, false, false, false);

    /** Fold when pressure reaches this (honba 3, or South-4 first place with a thin lead). */
    static final int FOLD_THRESHOLD = 6;

    private final int honba;
    private final int riichiSticks;
    private final int roundNumber;
    private final Wind roundWind;
    private final Integer playerScore;
    private final Integer rightScore;
    private final Integer oppositeScore;
    private final Integer leftScore;
    private final boolean alreadyRiichi;
    private final boolean opponentRiichi;
    private final boolean present;

    TableSituation(
            int honba,
            int riichiSticks,
            int roundNumber,
            Wind roundWind,
            Integer playerScore,
            Integer rightScore,
            Integer oppositeScore,
            Integer leftScore,
            boolean alreadyRiichi,
            boolean opponentRiichi,
            boolean present
    ) {
        this.honba = honba;
        this.riichiSticks = riichiSticks;
        this.roundNumber = roundNumber;
        this.roundWind = roundWind;
        this.playerScore = playerScore;
        this.rightScore = rightScore;
        this.oppositeScore = oppositeScore;
        this.leftScore = leftScore;
        this.alreadyRiichi = alreadyRiichi;
        this.opponentRiichi = opponentRiichi;
        this.present = present;
    }

    public static TableSituation from(HandRequest request) {
        if (request == null) {
            return NONE;
        }
        return from(
                request.getHonba(),
                request.getRiichiSticks(),
                request.getRoundNumber(),
                request.getPlayerScore(),
                request.getRightScore(),
                request.getOppositeScore(),
                request.getLeftScore(),
                request.getOpponents(),
                request.getPlayer(),
                request.getRoundWind()
        );
    }

    public static TableSituation from(CallDecisionRequest request) {
        if (request == null) {
            return NONE;
        }
        return from(
                request.getHonba(),
                request.getRiichiSticks(),
                request.getRoundNumber(),
                request.getPlayerScore(),
                request.getRightScore(),
                request.getOppositeScore(),
                request.getLeftScore(),
                request.getOpponents(),
                request.getPlayer(),
                request.getRoundWind()
        );
    }

    public static TableSituation from(
            Integer honba,
            Integer riichiSticks,
            Integer roundNumber,
            Integer playerScore,
            Integer rightScore,
            Integer oppositeScore,
            Integer leftScore,
            List<PlayerDiscardsDTO> opponents,
            PlayerDiscardsDTO player,
            Wind roundWind
    ) {
        boolean alreadyRiichi = player != null && player.isRiichi();
        boolean opponentRiichi = anyOpponentRiichi(opponents);
        boolean present = honba != null || riichiSticks != null || roundNumber != null
                || playerScore != null || rightScore != null || oppositeScore != null || leftScore != null
                || alreadyRiichi || opponentRiichi;
        if (!present) {
            return NONE;
        }
        return new TableSituation(
                honba != null ? honba : 0,
                riichiSticks != null ? riichiSticks : 0,
                roundNumber != null ? roundNumber : 0,
                roundWind,
                playerScore,
                rightScore,
                oppositeScore,
                leftScore,
                alreadyRiichi,
                opponentRiichi,
                true
        );
    }

    public int honba() {
        return honba;
    }

    public int riichiSticks() {
        return riichiSticks;
    }

    public int roundNumber() {
        return roundNumber;
    }

    public Wind roundWind() {
        return roundWind;
    }

    public Integer playerScore() {
        return playerScore;
    }

    public Integer rightScore() {
        return rightScore;
    }

    public Integer oppositeScore() {
        return oppositeScore;
    }

    public Integer leftScore() {
        return leftScore;
    }

    public boolean alreadyRiichi() {
        return alreadyRiichi;
    }

    public boolean opponentRiichi() {
        return opponentRiichi;
    }

    public boolean present() {
        return present;
    }

    public boolean preferDefense() {
        return alreadyRiichi || foldPressure() >= FOLD_THRESHOLD;
    }

    /**
     * Discard sort only. Danger moves ahead of ukeire and good-shape, still after shanten.
     * A single opponent riichi does this without flipping {@link #preferDefense()}
     * (honba, South-4 thin lead, and already-riichi still own damaten and skipped opens).
     */
    public boolean rankDangerBeforeOffense() {
        return preferDefense() || opponentRiichi;
    }

    /**
     * Skip declaring riichi when the hand already has yaku and the table is unstable.
     * Hands with no yaku still riichi (otherwise they cannot win).
     */
    public boolean cautiousRiichi() {
        return preferDefense();
    }

    /**
     * Skip opening calls that do not improve shanten when folding.
     */
    public boolean skipNonImprovingCalls() {
        return preferDefense();
    }

    public int foldPressure() {
        if (!present) {
            return 0;
        }
        int pressure = honba * 2 + riichiSticks * 2;
        if (isOras()) {
            pressure += 4;
        } else if (isApproachingOras()) {
            pressure += 2;
        }
        if (opponentRiichi) {
            pressure += 3;
        }
        if (alreadyRiichi) {
            pressure += 5;
        }
        if (hasScores()) {
            if (isFirstPlace()) {
                int lead = leadOverClosest();
                if (isLateRound() && lead <= 3900) {
                    pressure += 4;
                } else if (isLateRound() && lead <= 8000) {
                    pressure += 3;
                } else if (lead <= 3900 && (honba >= 2 || riichiSticks >= 2)) {
                    pressure += 3;
                }
            } else if (isLastPlace() && trailBehindThird() >= 12000) {
                pressure -= 6;
            }
        }
        return Math.max(0, pressure);
    }

    public boolean isFirstPlace() {
        if (playerScore == null) {
            return false;
        }
        for (Integer score : opponentScoreList()) {
            if (score != null && score > playerScore) {
                return false;
            }
        }
        return opponentScoreList().stream().anyMatch(s -> s != null);
    }

    public boolean isLastPlace() {
        if (playerScore == null) {
            return false;
        }
        boolean any = false;
        for (Integer score : opponentScoreList()) {
            if (score == null) {
                continue;
            }
            any = true;
            if (score < playerScore) {
                return false;
            }
        }
        return any;
    }

    public int leadOverClosest() {
        if (playerScore == null) {
            return 0;
        }
        Integer closest = null;
        for (Integer score : opponentScoreList()) {
            if (score == null) {
                continue;
            }
            if (closest == null || score > closest) {
                closest = score;
            }
        }
        return closest == null ? 0 : playerScore - closest;
    }

    public int trailBehindThird() {
        if (playerScore == null) {
            return 0;
        }
        List<Integer> others = new ArrayList<>();
        for (Integer score : opponentScoreList()) {
            if (score != null) {
                others.add(score);
            }
        }
        if (others.size() < 3) {
            return 0;
        }
        others.sort(Integer::compareTo);
        return others.get(0) - playerScore;
    }

    /**
     * {@code round_number} is kyoku within {@code round_wind} (typically 1–4), not a hanchan index.
     * South 3+ / West / North count as late for place protection.
     */
    public boolean isLateRound() {
        return isOras() || isApproachingOras();
    }

    public boolean hasScores() {
        return playerScore != null && opponentScoreList().stream().anyMatch(s -> s != null);
    }

    private boolean isOras() {
        if (roundWind == Wind.WEST || roundWind == Wind.NORTH) {
            return true;
        }
        return roundWind == Wind.SOUTH && roundNumber >= 4;
    }

    private boolean isApproachingOras() {
        return roundWind == Wind.SOUTH && roundNumber == 3;
    }

    private List<Integer> opponentScoreList() {
        return List.of(rightScore, oppositeScore, leftScore);
    }

    private static boolean anyOpponentRiichi(List<PlayerDiscardsDTO> opponents) {
        if (opponents == null) {
            return false;
        }
        for (PlayerDiscardsDTO opponent : opponents) {
            if (opponent != null && opponent.isRiichi()) {
                return true;
            }
        }
        return false;
    }
}
