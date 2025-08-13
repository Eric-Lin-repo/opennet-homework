package com.example.demo.service;

import com.example.demo.dto.RecentNotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    
    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;
    private static final String KEY = "notifications:recent";
    private static final int MAX_SIZE = 10;
    private static final int TTL_MINUTES = 10;
    
    public void add(RecentNotificationResponse n) {
        if (n == null || n.getId() == null) return;
        
        try {
            ListOperations<String, Object> ops = redis.opsForList();
            ops.leftPush(KEY, n);
            ops.trim(KEY, 0, MAX_SIZE - 1);
            redis.expire(KEY, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("RedisService add Error: {}", e.getMessage());
        }
    }
    
    public void update(RecentNotificationResponse n) {
        if (n == null || n.getId() == null) return;
        
        try {
            remove(n.getId());
            add(n);
        } catch (Exception e) {
            log.error("RedisService update Error: {}", e.getMessage());
        }
    }
    
    public List<RecentNotificationResponse> get() {
        try {
            List<Object> items = redis.opsForList().range(KEY, 0, MAX_SIZE - 1);
            if (items == null || items.isEmpty()) return new ArrayList<>();
            
            List<RecentNotificationResponse> result = new ArrayList<>();
            for (Object item : items) {
                try {
                    RecentNotificationResponse response = objectMapper.convertValue(item, RecentNotificationResponse.class);
                    result.add(response);
                } catch (Exception e) {
                    log.error("Failed to convert item: {}, error: {}", item, e.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("RedisService get Error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void remove(Long id) {
        if (id == null) return;
        
        try {
            List<Object> items = redis.opsForList().range(KEY, 0, -1);
            if (items == null || items.isEmpty()) return;
            
            for (Object item : items) {
                try {
                    RecentNotificationResponse n = objectMapper.convertValue(item, RecentNotificationResponse.class);
                    if (n.getId().equals(id)) {
                        redis.opsForList().remove(KEY, 1, item);
                        return;
                    }
                } catch (Exception e) {
                    log.error("Failed to process item during remove: {}, error: {}", item, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("RedisService remove Error: {}", e.getMessage());
        }
    }

}