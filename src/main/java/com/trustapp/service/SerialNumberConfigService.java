package com.trustapp.service;

import com.trustapp.dto.SerialNumberConfigCreateDTO;
import com.trustapp.dto.SerialNumberConfigDTO;
import com.trustapp.dto.SerialNumberConfigUpdateDTO;
import com.trustapp.dto.SerialNumberNextResponseDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.repository.SerialNumberConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SerialNumberConfigService {
    
    private final SerialNumberConfigRepository serialNumberConfigRepository;
    
    public SerialNumberConfigService(SerialNumberConfigRepository serialNumberConfigRepository) {
        this.serialNumberConfigRepository = serialNumberConfigRepository;
    }
    
    public List<SerialNumberConfigDTO> getAllConfigurations() {
        return serialNumberConfigRepository.findAll();
    }
    
    public SerialNumberConfigDTO getConfigurationById(Long id) {
        return serialNumberConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serial number config not found with id: " + id));
    }
    
    public SerialNumberConfigDTO getConfigurationByEntityType(String entityType) {
        return serialNumberConfigRepository.findByEntityType(entityType)
            .orElseThrow(() -> new ResourceNotFoundException("Serial number config not found for entity: " + entityType));
    }
    
    @Transactional
    public SerialNumberConfigDTO createConfiguration(SerialNumberConfigCreateDTO createDTO) {
        // Check for duplicate entity type
        if (serialNumberConfigRepository.existsByEntityType(createDTO.getEntityType())) {
            throw new DuplicateResourceException("Serial number config already exists for entity type: " + createDTO.getEntityType());
        }
        
        // Convert CreateDTO to DTO for saving
        SerialNumberConfigDTO configDTO = new SerialNumberConfigDTO();
        configDTO.setEntityType(createDTO.getEntityType());
        configDTO.setPrefix(createDTO.getPrefix());
        configDTO.setFormatPattern(createDTO.getFormatPattern() != null ? createDTO.getFormatPattern() : "{PREFIX}-{YEAR}-{SEQUENCE}");
        configDTO.setCurrentYear(createDTO.getCurrentYear());
        configDTO.setLastSequence(createDTO.getLastSequence() != null ? createDTO.getLastSequence() : 0);
        configDTO.setSequenceLength(createDTO.getSequenceLength() != null ? createDTO.getSequenceLength() : 4);
        
        Long id = serialNumberConfigRepository.save(configDTO);
        return getConfigurationById(id);
    }
    
    @Transactional
    public SerialNumberConfigDTO updateConfiguration(Long id, SerialNumberConfigUpdateDTO updateDTO) {
        // Check if configuration exists
        SerialNumberConfigDTO existingConfig = getConfigurationById(id);
        
        // Convert UpdateDTO to DTO for updating
        SerialNumberConfigDTO configDTO = new SerialNumberConfigDTO();
        configDTO.setPrefix(updateDTO.getPrefix());
        configDTO.setFormatPattern(updateDTO.getFormatPattern() != null ? updateDTO.getFormatPattern() : existingConfig.getFormatPattern());
        configDTO.setSequenceLength(updateDTO.getSequenceLength() != null ? updateDTO.getSequenceLength() : existingConfig.getSequenceLength());
        
        serialNumberConfigRepository.update(id, configDTO);
        return getConfigurationById(id);
    }
    
    @Transactional
    public SerialNumberNextResponseDTO getNextSerialNumber(String entityType) {
        try {
            // Get the next serial number (this will increment the sequence)
            String serialNumber = serialNumberConfigRepository.getNextSerialNumber(entityType);
            
            // Get updated config to get the new sequence value
            SerialNumberConfigDTO updatedConfig = serialNumberConfigRepository.findByEntityType(entityType)
                .orElseThrow(() -> new ResourceNotFoundException("Serial number config not found for entity: " + entityType));
            
            SerialNumberNextResponseDTO response = new SerialNumberNextResponseDTO();
            response.setSerialNumber(serialNumber);
            response.setEntityType(entityType);
            response.setYear(updatedConfig.getCurrentYear());
            response.setSequence(updatedConfig.getLastSequence());
            
            return response;
        } catch (IllegalStateException e) {
            throw new ResourceNotFoundException("Serial number config not found for entity: " + entityType);
        }
    }
}

