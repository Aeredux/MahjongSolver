package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mahjong.model.CallType;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallDecisionRequest {
    private List<TileType> hand;
    private TileType calledTile;
    private CallType callType;
    private List<TileType> sequenceTiles;
    private boolean menzen;
    private int playerScore;
    private boolean openKan;
    private List<PlayerDiscardsDTO> opponents;

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
}
