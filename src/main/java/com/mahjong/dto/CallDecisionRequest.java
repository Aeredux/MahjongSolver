package com.mahjong.dto;

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
    private Wind seatWind;
    private Wind roundWind;
}
