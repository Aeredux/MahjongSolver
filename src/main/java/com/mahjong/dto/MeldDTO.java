package com.mahjong.dto;

import com.mahjong.model.MeldType;
import com.mahjong.model.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeldDTO {
    private MeldType type;
    private List<TileType> tiles;
}
