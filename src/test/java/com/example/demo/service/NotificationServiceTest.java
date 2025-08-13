package com.example.demo.service;

import com.example.demo.constants.NotificationType;
import com.example.demo.dto.CreateNotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.dto.RecentNotificationResponse;
import com.example.demo.dto.UpdateNotificationRequest;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.mq.RocketMQService;
import com.example.demo.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private RocketMQService rocketMQService;

    @InjectMocks
    private NotificationService notificationService;

    private Notification mockNotification;
    private CreateNotificationRequest createRequest;
    private UpdateNotificationRequest updateRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        mockNotification = Notification.builder()
                .id(1L)
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .createdAt(now)
                .updatedAt(now)
                .build();

        createRequest = CreateNotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .build();

        updateRequest = UpdateNotificationRequest.builder()
                .subject("Updated Subject")
                .content("Updated Content")
                .build();
    }

    @Test
    void createNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);
        doNothing().when(redisService).add(any(RecentNotificationResponse.class));
        doNothing().when(rocketMQService).send(any(NotificationResponse.class));

        NotificationResponse response = notificationService.createNotification(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(NotificationType.EMAIL, response.getType());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals("Test Subject", response.getSubject());
        assertEquals("Test Content", response.getContent());

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(redisService, times(1)).add(any(RecentNotificationResponse.class));
        verify(rocketMQService, times(1)).send(any(NotificationResponse.class));
    }

    @Test
    void getNotificationById_ExistingId_ReturnsNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(mockNotification));

        NotificationResponse response = notificationService.getNotificationById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getRecipient());

        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void getNotificationById_NonExistingId_ThrowsException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.getNotificationById(999L);
        });

        verify(notificationRepository, times(1)).findById(999L);
    }

    @Test
    void getRecentNotifications_WithCachedData_ReturnsCachedList() {
        List<RecentNotificationResponse> cachedList = Arrays.asList(
                RecentNotificationResponse.builder()
                        .id(1L)
                        .type(NotificationType.EMAIL)
                        .recipient("cached@example.com")
                        .subject("Cached Subject")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(redisService.get()).thenReturn(cachedList);

        List<RecentNotificationResponse> result = notificationService.getRecentNotifications();

        assertEquals(1, result.size());
        assertEquals("cached@example.com", result.get(0).getRecipient());

        verify(redisService, times(1)).get();
        verify(notificationRepository, never()).findTop10ByOrderByCreatedAtDesc();
    }

    @Test
    void getRecentNotifications_NoCachedData_FetchesFromDatabase() {
        when(redisService.get()).thenReturn(Collections.emptyList());
        
        List<Notification> dbNotifications = Arrays.asList(
                mockNotification,
                Notification.builder()
                        .id(2L)
                        .type(NotificationType.SMS)
                        .recipient("+1234567890")
                        .subject("SMS Subject")
                        .content("SMS Content")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
        
        when(notificationRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(dbNotifications);
        doNothing().when(redisService).add(any(RecentNotificationResponse.class));

        List<RecentNotificationResponse> result = notificationService.getRecentNotifications();

        assertEquals(2, result.size());
        assertEquals("test@example.com", result.get(0).getRecipient());
        assertEquals("+1234567890", result.get(1).getRecipient());

        verify(redisService, times(1)).get();
        verify(notificationRepository, times(1)).findTop10ByOrderByCreatedAtDesc();
        verify(redisService, times(2)).add(any(RecentNotificationResponse.class));
    }

    @Test
    void updateNotification_ExistingId_UpdatesSuccessfully() {
        Notification updatedNotification = Notification.builder()
                .id(1L)
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Updated Subject")
                .content("Updated Content")
                .createdAt(mockNotification.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(updatedNotification);
        doNothing().when(redisService).update(any(RecentNotificationResponse.class));

        NotificationResponse response = notificationService.updateNotification(1L, updateRequest);

        assertEquals("Updated Subject", response.getSubject());
        assertEquals("Updated Content", response.getContent());

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(redisService, times(1)).update(any(RecentNotificationResponse.class));
    }

    @Test
    void updateNotification_NonExistingId_ThrowsException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.updateNotification(999L, updateRequest);
        });

        verify(notificationRepository, times(1)).findById(999L);
        verify(notificationRepository, never()).save(any());
        verify(redisService, never()).update(any());
    }

    @Test
    void updateNotification_EmptyFields_DoesNotUpdate() {
        UpdateNotificationRequest emptyUpdate = UpdateNotificationRequest.builder()
                .subject("   ")
                .content("   ")
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        NotificationResponse response = notificationService.updateNotification(1L, emptyUpdate);

        assertEquals("Test Subject", response.getSubject());
        assertEquals("Test Content", response.getContent());
    }

    @Test
    void deleteNotification_ExistingId_DeletesSuccessfully() {
        when(notificationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(1L);
        doNothing().when(redisService).remove(1L);

        assertDoesNotThrow(() -> {
            notificationService.deleteNotification(1L);
        });

        verify(notificationRepository, times(1)).existsById(1L);
        verify(notificationRepository, times(1)).deleteById(1L);
        verify(redisService, times(1)).remove(1L);
    }

    @Test
    void deleteNotification_NonExistingId_ThrowsException() {
        when(notificationRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.deleteNotification(999L);
        });

        verify(notificationRepository, times(1)).existsById(999L);
        verify(notificationRepository, never()).deleteById(any());
        verify(redisService, never()).remove(any());
    }

}