package com.example.email.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
        private String name;
        private String email;
        private LocalDateTime dateCreated = LocalDateTime.now();
}
