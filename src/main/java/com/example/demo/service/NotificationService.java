package com.example.demo.service;

import com.example.demo.dto.CreateNotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.dto.RecentNotificationResponse;
import com.example.demo.dto.UpdateNotificationRequest;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.mq.RocketMQService;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisService redisService;
    private final RocketMQService rocketMQService;


    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .type(request.getType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = mapToResponse(saved);

        RecentNotificationResponse recentResponse = mapToRecentResponse(saved);
        redisService.add(recentResponse);
        rocketMQService.send(response);
        return response;
    }

    @Cacheable(value = "notification", key = "#id")
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        return mapToResponse(notification);
    }

    public List<RecentNotificationResponse> getRecentNotifications() {
        List<RecentNotificationResponse> cached = redisService.get();
        if (!cached.isEmpty()) {
            return cached;
        }

        List<RecentNotificationResponse> dbRecent = notificationRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::mapToRecentResponse)
                .collect(Collectors.toList());

        dbRecent.forEach(redisService::add);

        return dbRecent;
    }

    @Transactional
    @CachePut(value = "notification", key = "#id")
    public NotificationResponse updateNotification(Long id, UpdateNotificationRequest request) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        if (request.getSubject() != null && !request.getSubject().trim().isEmpty()) {
            notification.setSubject(request.getSubject());
        }
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            notification.setContent(request.getContent());
        }

        Notification updated = notificationRepository.save(notification);
        NotificationResponse response = mapToResponse(updated);

        RecentNotificationResponse recentResponse = mapToRecentResponse(updated);
        redisService.update(recentResponse);

        return response;
    }

    @Transactional
    @CacheEvict(value = "notification", key = "#id")
    public void deleteNotification(Long id) {

        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }

        notificationRepository.deleteById(id);
        redisService.remove(id);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    private RecentNotificationResponse mapToRecentResponse(Notification notification) {
        return RecentNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
