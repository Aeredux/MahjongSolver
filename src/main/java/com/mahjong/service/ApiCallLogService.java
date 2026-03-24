package com.mahjong.service;

import com.mahjong.entity.ApiCallLog;
import com.mahjong.repository.ApiCallLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApiCallLogService {

    private static final Logger logger = LoggerFactory.getLogger(ApiCallLogService.class);

    @Autowired
    private ApiCallLogRepository apiCallLogRepository;

    public ApiCallLog log(String endpoint, String httpMethod, String requestBody,
                          int responseStatus, long durationMs, String errorMessage) {
        ApiCallLog record = new ApiCallLog();
        record.setEndpoint(endpoint);
        record.setHttpMethod(httpMethod);
        record.setRequestBody(requestBody);
        record.setResponseStatus(responseStatus);
        record.setDurationMs(durationMs);
        record.setErrorMessage(errorMessage);

        ApiCallLog saved = apiCallLogRepository.save(record);
        logger.debug("Logged API call id={} {} {} -> {} in {}ms",
                saved.getId(), httpMethod, endpoint, responseStatus, durationMs);
        return saved;
    }

    public List<ApiCallLog> findAll() {
        return apiCallLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ApiCallLog> findByEndpoint(String endpoint) {
        return apiCallLogRepository.findByEndpointOrderByCreatedAtDesc(endpoint);
    }

    public Optional<ApiCallLog> findById(Long id) {
        return apiCallLogRepository.findById(id);
    }
}
