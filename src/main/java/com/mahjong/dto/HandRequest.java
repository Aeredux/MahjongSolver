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
}
