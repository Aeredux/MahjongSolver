package com.mahjong.service;

import com.mahjong.entity.GameHistory;
import com.mahjong.model.MoveSuggestion;
import com.mahjong.model.TileType;
import com.mahjong.repository.GameHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(GameHistoryService.class);

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    public GameHistory saveGameHistory(List<TileType> hand, TileType drawnTile,
                                       int currentShanten, List<MoveSuggestion> suggestions) {
        GameHistory record = new GameHistory();
        record.setHandTiles(hand.stream().map(Enum::name).collect(Collectors.joining(",")));
        record.setDrawnTile(drawnTile != null ? drawnTile.name() : null);
        record.setCurrentShanten(currentShanten);
        record.setSuggestionCount(suggestions.size());

        if (!suggestions.isEmpty()) {
            MoveSuggestion best = suggestions.get(0);
            record.setBestDiscard(best.getDiscardTile() != null ? best.getDiscardTile().name() : null);
            record.setBestConfidence(best.getConfidence());
        }

        GameHistory saved = gameHistoryRepository.save(record);
        logger.debug("Saved game history record id={}, shanten={}, bestDiscard={}",
                saved.getId(), saved.getCurrentShanten(), saved.getBestDiscard());
        return saved;
    }

    public List<GameHistory> findAll() {
        return gameHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<GameHistory> findRecent(int limit) {
        return gameHistoryRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    public Optional<GameHistory> findById(Long id) {
        return gameHistoryRepository.findById(id);
    }
}
