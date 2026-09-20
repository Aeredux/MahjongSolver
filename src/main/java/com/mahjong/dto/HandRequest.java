package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandRequest {
    private List<TileType> hand;
    private TileType drawnTile;
    private List<PlayerDiscardsDTO> opponents;
    private List<TileType> discardTiles;

    @JsonAlias({"seatWind", "seat_wind"})
    private Wind seatWind;

    @JsonAlias({"roundWind", "round_wind"})
    private Wind roundWind;

    /** Doman panel tiles as displayed — the panel tile IS the dora (no Tenhou +1). */
    private List<TileType> dora;

    /** Caller's own open melds. */
    private List<MeldDTO> melds;

    /** Optional same shape as an opponents[] entry (pond + tsumogiri + melds + riichi). */
    private PlayerDiscardsDTO player;

    /** Repeat counters and sticks (KAN-92). Absent means "not sent". */
    private Integer honba;
    private Integer riichiSticks;
    private Integer roundNumber;

    /** Own score; also accepted on suggest-move (evaluate-call already had player_score). */
    private Integer playerScore;

    /** Seat-relative opponent scores aligned with opponents[]: right / opposite / left. */
    private Integer rightScore;
    private Integer oppositeScore;
    private Integer leftScore;
    private List<Integer> opponentScores;
}
