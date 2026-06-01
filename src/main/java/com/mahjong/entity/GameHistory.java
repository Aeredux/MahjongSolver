package com.mahjong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "hand_tiles", nullable = false)
    private String handTiles;

    @Column(name = "drawn_tile")
    private String drawnTile;

    @Column(name = "current_shanten", nullable = false)
    private int currentShanten;

    @Column(name = "best_discard")
    private String bestDiscard;

    @Column(name = "best_confidence")
    private double bestConfidence;

    @Column(name = "suggestion_count", nullable = false)
    private int suggestionCount;

    @Column(name = "discard_tiles")
    private String discardTiles;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
