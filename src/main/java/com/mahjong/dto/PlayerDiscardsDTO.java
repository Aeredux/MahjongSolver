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

    /** Optional seat score; opponents[] order is right / opposite / left. */
    private Integer score;

    public PlayerDiscardsDTO(Wind wind, List<DiscardedTileDTO> discards, boolean riichi, List<MeldDTO> melds) {
        this.wind = wind;
        this.discards = discards;
        this.riichi = riichi;
        this.melds = melds;
    }
}
