package com.mahjong.repository;

import com.mahjong.entity.ApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long> {

    List<ApiCallLog> findAllByOrderByCreatedAtDesc();

    List<ApiCallLog> findByEndpointOrderByCreatedAtDesc(String endpoint);
}
