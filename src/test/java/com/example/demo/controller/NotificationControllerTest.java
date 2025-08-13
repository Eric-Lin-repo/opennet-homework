package com.example.demo.controller;

import com.example.demo.constants.NotificationType;
import com.example.demo.dto.CreateNotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.dto.RecentNotificationResponse;
import com.example.demo.dto.UpdateNotificationRequest;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponse mockResponse;
    private CreateNotificationRequest createRequest;
    private UpdateNotificationRequest updateRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        mockResponse = NotificationResponse.builder()
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
    void createNotification_Success() throws Exception {
        when(notificationService.createNotification(any(CreateNotificationRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.recipient").value("test@example.com"))
                .andExpect(jsonPath("$.subject").value("Test Subject"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(notificationService, times(1)).createNotification(any(CreateNotificationRequest.class));
    }

    @Test
    void createNotification_InvalidInput_ReturnsBadRequest() throws Exception {
        CreateNotificationRequest invalidRequest = CreateNotificationRequest.builder()
                .type(null) // Missing required field
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .build();

        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void createNotification_WithSmsType_Success() throws Exception {
        CreateNotificationRequest smsRequest = CreateNotificationRequest.builder()
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .subject("SMS Subject")
                .content("SMS Content")
                .build();

        NotificationResponse smsResponse = NotificationResponse.builder()
                .id(2L)
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .subject("SMS Subject")
                .content("SMS Content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(notificationService.createNotification(any(CreateNotificationRequest.class)))
                .thenReturn(smsResponse);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("SMS"))
                .andExpect(jsonPath("$.recipient").value("+1234567890"));
    }

    @Test
    void createNotification_EmptyRecipient_ReturnsBadRequest() throws Exception {
        CreateNotificationRequest invalidRequest = CreateNotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("") // Empty recipient
                .subject("Test Subject")
                .content("Test Content")
                .build();

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotificationById_Success() throws Exception {
        when(notificationService.getNotificationById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.recipient").value("test@example.com"));

        verify(notificationService, times(1)).getNotificationById(1L);
    }

    @Test
    void getNotificationById_NotFound() throws Exception {
        when(notificationService.getNotificationById(999L))
                .thenThrow(new NotificationNotFoundException(999L));

        mockMvc.perform(get("/notifications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        verify(notificationService, times(1)).getNotificationById(999L);
    }

    @Test
    void getRecentNotifications_Success() throws Exception {
        List<RecentNotificationResponse> recentList = Arrays.asList(
                RecentNotificationResponse.builder()
                        .id(1L)
                        .type(NotificationType.EMAIL)
                        .recipient("test1@example.com")
                        .subject("Subject 1")
                        .createdAt(LocalDateTime.now())
                        .build(),
                RecentNotificationResponse.builder()
                        .id(2L)
                        .type(NotificationType.SMS)
                        .recipient("test2@example.com")
                        .subject("Subject 2")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(notificationService.getRecentNotifications()).thenReturn(recentList);

        mockMvc.perform(get("/notifications/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("EMAIL"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("SMS"));

        verify(notificationService, times(1)).getRecentNotifications();
    }

    @Test
    void updateNotification_Success() throws Exception {
        NotificationResponse updatedResponse = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Updated Subject")
                .content("Updated Content")
                .createdAt(mockResponse.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(notificationService.updateNotification(eq(1L), any(UpdateNotificationRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/notifications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.subject").value("Updated Subject"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(notificationService, times(1)).updateNotification(eq(1L), any(UpdateNotificationRequest.class));
    }

    @Test
    void updateNotification_NotFound() throws Exception {
        when(notificationService.updateNotification(eq(999L), any(UpdateNotificationRequest.class)))
                .thenThrow(new NotificationNotFoundException(999L));

        mockMvc.perform(put("/notifications/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        verify(notificationService, times(1)).updateNotification(eq(999L), any(UpdateNotificationRequest.class));
    }

    @Test
    void updateNotification_ValidRequest_Success() throws Exception {
        UpdateNotificationRequest validUpdate = UpdateNotificationRequest.builder()
                .subject("Updated Subject")
                .content("Updated Content")
                .build();

        when(notificationService.updateNotification(eq(1L), any(UpdateNotificationRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(put("/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotification_InvalidRequest_ReturnsBadRequest() throws Exception {
        UpdateNotificationRequest invalidRequest = UpdateNotificationRequest.builder()
                .subject("")
                .content("")
                .build();

        mockMvc.perform(put("/notifications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).updateNotification(any(), any());
    }

    @Test
    void deleteNotification_Success() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).deleteNotification(1L);
    }

    @Test
    void deleteNotification_NotFound() throws Exception {
        doThrow(new NotificationNotFoundException(999L)).when(notificationService).deleteNotification(999L);

        mockMvc.perform(delete("/notifications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        verify(notificationService, times(1)).deleteNotification(999L);
    }


}