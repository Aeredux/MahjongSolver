package com.mahjong.repository;

import com.mahjong.entity.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {

    List<GameHistory> findAllByOrderByCreatedAtDesc();
}
