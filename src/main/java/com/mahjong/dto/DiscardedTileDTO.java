package com.mahjong.dto;

import com.mahjong.model.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscardedTileDTO {
    private TileType tile;
    private boolean tsumogiri;
}
