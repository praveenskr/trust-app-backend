package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String module;
    private String resource;
    private String action;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

