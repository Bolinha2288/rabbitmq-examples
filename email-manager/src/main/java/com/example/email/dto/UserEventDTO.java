package com.example.email.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserEventDTO {
    private UserDTO userDTO;
    private String eventType;
    private LocalDateTime eventDate;
}
