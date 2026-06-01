package com.mahjong.dto;

import com.mahjong.model.Wind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDiscardsDTO {
    private Wind wind;
    private List<DiscardedTileDTO> discards;
    private boolean riichi;
    private List<MeldDTO> melds;
}
