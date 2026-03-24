package com.mahjong.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meld {
    private MeldType type;
    private List<Tile> tiles;
    private Wind calledFrom;

    public boolean isOpen() {
        return type != MeldType.KAN_CLOSED;
    }

    public boolean isClosed() {
        return type == MeldType.KAN_CLOSED;
    }

    public boolean isKan() {
        return type == MeldType.KAN_OPEN || type == MeldType.KAN_CLOSED || type == MeldType.KAN_ADDED;
    }
}
