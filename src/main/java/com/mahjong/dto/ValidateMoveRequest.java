package com.mahjong.dto;

import com.mahjong.model.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateMoveRequest {
    private List<TileType> hand;
    private TileType discardTile;
    private TileType drawnTile;
    private boolean riichi;
}
