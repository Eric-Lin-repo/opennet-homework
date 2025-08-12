package com.example.demo.dto;

import com.example.demo.constants.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentNotificationResponse {
    
    private Long id;
    
    private NotificationType type;
    
    private String recipient;
    
    private String subject;
    
    private LocalDateTime createdAt;
}