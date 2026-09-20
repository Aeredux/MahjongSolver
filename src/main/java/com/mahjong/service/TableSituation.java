package com.mahjong.service;

import com.mahjong.dto.CallDecisionRequest;
import com.mahjong.dto.HandRequest;
import com.mahjong.dto.PlayerDiscardsDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Honba / riichi sticks / round / table scores for push-vs-fold.
 * Missing fields stay absent so existing clients keep today's ranking.
 */
public final class TableSituation {

    public static final TableSituation NONE = new TableSituation(
            0, 0, 0, null, null, null, null, false, false, false);

    /** Fold when pressure reaches this (honba 3, or late first-place with a thin lead). */
    static final int FOLD_THRESHOLD = 6;

    private final int honba;
    private final int riichiSticks;
    private final int roundNumber;
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
                request.getOpponentScores(),
                request.getOpponents(),
                request.getPlayer()
        );
    }

    public static TableSituation from(CallDecisionRequest request) {
        if (request == null) {
            return NONE;
        }
        Integer playerScore = request.getPlayerScore() > 0 ? request.getPlayerScore() : null;
        return from(
                request.getHonba(),
                request.getRiichiSticks(),
                request.getRoundNumber(),
                playerScore,
                request.getRightScore(),
                request.getOppositeScore(),
                request.getLeftScore(),
                request.getOpponentScores(),
                request.getOpponents(),
                request.getPlayer()
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
            List<Integer> opponentScores,
            List<PlayerDiscardsDTO> opponents,
            PlayerDiscardsDTO player
    ) {
        boolean alreadyRiichi = player != null && player.isRiichi();
        boolean opponentRiichi = anyOpponentRiichi(opponents);
        List<Integer> resolved = resolveOpponentScores(
                opponentScores, opponents, rightScore, oppositeScore, leftScore);
        Integer right = resolved.size() > 0 ? resolved.get(0) : rightScore;
        Integer opposite = resolved.size() > 1 ? resolved.get(1) : oppositeScore;
        Integer left = resolved.size() > 2 ? resolved.get(2) : leftScore;

        boolean present = honba != null || riichiSticks != null || roundNumber != null
                || playerScore != null || right != null || opposite != null || left != null
                || alreadyRiichi;
        if (!present) {
            return NONE;
        }
        return new TableSituation(
                honba != null ? honba : 0,
                riichiSticks != null ? riichiSticks : 0,
                roundNumber != null ? roundNumber : 0,
                playerScore,
                right,
                opposite,
                left,
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
        if (roundNumber >= 8) {
            pressure += 4;
        } else if (roundNumber >= 7) {
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

    public boolean isLateRound() {
        return roundNumber >= 7;
    }

    public boolean hasScores() {
        return playerScore != null && opponentScoreList().stream().anyMatch(s -> s != null);
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

    /**
     * Seat-relative: index 0 = right (shimocha), 1 = opposite, 2 = left (kamicha),
     * matching Helper {@code opponents[]}.
     */
    static List<Integer> resolveOpponentScores(
            List<Integer> opponentScores,
            List<PlayerDiscardsDTO> opponents,
            Integer rightScore,
            Integer oppositeScore,
            Integer leftScore
    ) {
        List<Integer> resolved = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            Integer fromList = opponentScores != null && i < opponentScores.size()
                    ? opponentScores.get(i) : null;
            Integer fromOpponent = null;
            if (opponents != null && i < opponents.size() && opponents.get(i) != null) {
                fromOpponent = opponents.get(i).getScore();
            }
            Integer fromFlat = switch (i) {
                case 0 -> rightScore;
                case 1 -> oppositeScore;
                default -> leftScore;
            };
            Integer value = fromList != null ? fromList : fromOpponent != null ? fromOpponent : fromFlat;
            resolved.add(value);
        }
        return resolved;
    }
}
