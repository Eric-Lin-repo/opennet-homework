package com.example.demo.service;

import com.example.demo.dto.CreateNotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.dto.RecentNotificationResponse;
import com.example.demo.dto.UpdateNotificationRequest;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .type(request.getType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .build();

        return mapToResponse(notificationRepository.save(notification));
    }

    public NotificationResponse getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    public List<RecentNotificationResponse> getRecentNotifications() {
        return notificationRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::mapToRecentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse updateNotification(Long id, UpdateNotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        if (request.getSubject() != null) notification.setSubject(request.getSubject());
        if (request.getContent() != null) notification.setContent(request.getContent());

        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }

        notificationRepository.deleteById(id);
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
