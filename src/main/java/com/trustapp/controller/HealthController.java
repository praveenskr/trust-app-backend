package com.trustapp.controller;

import com.trustapp.dto.HealthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthResponseDTO> health() {
        HealthResponseDTO response = new HealthResponseDTO();
        response.setStatus("UP");
        response.setTimestamp(LocalDateTime.now());
        response.setApplication("Trust Management System");
        response.setVersion("1.0.0");
        return ResponseEntity.ok(response);
    }
}

