package com.example.demo.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationRequest {
    
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;
    
    @Size(max = 5000, message = "Content cannot exceed 5000 characters")
    private String content;
}