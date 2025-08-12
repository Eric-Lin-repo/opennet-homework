package com.example.demo.dto;

import com.example.demo.constants.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    
    @NotNull(message = "Type is required")
    private NotificationType type;
    
    @NotBlank(message = "Recipient is required")
    @Size(max = 255, message = "Recipient cannot exceed 255 characters")
    private String recipient;
    
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;
    
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content cannot exceed 5000 characters")
    private String content;
}