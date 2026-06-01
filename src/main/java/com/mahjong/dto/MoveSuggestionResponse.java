package com.mahjong.dto;

import com.mahjong.model.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveSuggestionResponse {
    private List<MoveSuggestionDTO> suggestions;
    private int currentShanten;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MoveSuggestionDTO {
        private TileType discardTile;
        private int shantenAfterDiscard;
        private double confidence;
        private String reasoning;
        private int ukeireCount;
    }
}
