package com.mahjong.dto;

import com.mahjong.model.TileType;
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
}
