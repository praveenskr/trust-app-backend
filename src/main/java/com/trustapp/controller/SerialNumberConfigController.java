package com.trustapp.controller;

import com.trustapp.dto.SerialNumberConfigCreateDTO;
import com.trustapp.dto.SerialNumberConfigDTO;
import com.trustapp.dto.SerialNumberConfigUpdateDTO;
import com.trustapp.dto.SerialNumberNextResponseDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.SerialNumberConfigService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/serial-config")
public class SerialNumberConfigController {
    
    private final SerialNumberConfigService serialNumberConfigService;
    
    public SerialNumberConfigController(SerialNumberConfigService serialNumberConfigService) {
        this.serialNumberConfigService = serialNumberConfigService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SerialNumberConfigDTO>>> getAllConfigurations() {
        List<SerialNumberConfigDTO> configurations = serialNumberConfigService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success(configurations));
    }
    
    @GetMapping("/{entityType}")
    public ResponseEntity<ApiResponse<SerialNumberConfigDTO>> getConfigurationByEntityType(
            @PathVariable String entityType) {
        SerialNumberConfigDTO configuration = serialNumberConfigService.getConfigurationByEntityType(entityType);
        return ResponseEntity.ok(ApiResponse.success(configuration));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SerialNumberConfigDTO>> createConfiguration(
            @Valid @RequestBody SerialNumberConfigCreateDTO createDTO) {
        SerialNumberConfigDTO created = serialNumberConfigService.createConfiguration(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Serial number configuration created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SerialNumberConfigDTO>> updateConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody SerialNumberConfigUpdateDTO updateDTO) {
        SerialNumberConfigDTO updated = serialNumberConfigService.updateConfiguration(id, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Serial number configuration updated successfully", updated));
    }
    
    @GetMapping("/next/{entity}")
    public ResponseEntity<ApiResponse<SerialNumberNextResponseDTO>> getNextSerialNumber(
            @PathVariable String entity) {
        SerialNumberNextResponseDTO response = serialNumberConfigService.getNextSerialNumber(entity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

