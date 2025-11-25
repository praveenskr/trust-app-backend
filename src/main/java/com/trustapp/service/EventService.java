package com.trustapp.service;

import com.trustapp.dto.EventCreateDTO;
import com.trustapp.dto.EventDTO;
import com.trustapp.dto.EventUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.BranchRepository;
import com.trustapp.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {
    
    private final EventRepository eventRepository;
    private final BranchRepository branchRepository;
    
    public EventService(EventRepository eventRepository, BranchRepository branchRepository) {
        this.eventRepository = eventRepository;
        this.branchRepository = branchRepository;
    }
    
    public List<EventDTO> getAllEvents(Long branchId, String status, boolean includeInactive) {
        return eventRepository.findAll(branchId, status, includeInactive);
    }
    
    public EventDTO getEventById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }
    
    @Transactional
    public EventDTO createEvent(EventCreateDTO createDTO, Long userId) {
        // Check for duplicate code
        if (eventRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Event code already exists: " + createDTO.getCode());
        }
        
        // Validate branch exists if branchId is provided
        if (createDTO.getBranchId() != null) {
            branchRepository.findById(createDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + createDTO.getBranchId()));
        }
        
        // Validate date range: endDate must be after or equal to startDate
        if (createDTO.getEndDate() != null && createDTO.getStartDate() != null) {
            if (createDTO.getEndDate().isBefore(createDTO.getStartDate())) {
                throw new ValidationException("End date must be after or equal to start date");
            }
        }
        
        // Convert CreateDTO to DTO for saving
        EventDTO eventDTO = new EventDTO();
        eventDTO.setCode(createDTO.getCode());
        eventDTO.setName(createDTO.getName());
        eventDTO.setDescription(createDTO.getDescription());
        eventDTO.setStartDate(createDTO.getStartDate());
        eventDTO.setEndDate(createDTO.getEndDate());
        eventDTO.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : "PLANNED");
        eventDTO.setBranchId(createDTO.getBranchId());
        eventDTO.setIsActive(createDTO.getIsActive());
        
        Long id = eventRepository.save(eventDTO, userId);
        return getEventById(id);
    }
    
    @Transactional
    public EventDTO updateEvent(Long id, EventUpdateDTO updateDTO, Long userId) {
        // Check if event exists
        EventDTO existingEvent = getEventById(id);
        
        // Validate branch exists if branchId is provided
        if (updateDTO.getBranchId() != null) {
            branchRepository.findById(updateDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + updateDTO.getBranchId()));
        }
        
        // Validate date range: endDate must be after or equal to startDate
        if (updateDTO.getEndDate() != null && updateDTO.getStartDate() != null) {
            if (updateDTO.getEndDate().isBefore(updateDTO.getStartDate())) {
                throw new ValidationException("End date must be after or equal to start date");
            }
        }
        
        // Convert UpdateDTO to DTO for updating
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(id);
        eventDTO.setCode(existingEvent.getCode()); // Code is not updatable
        eventDTO.setName(updateDTO.getName());
        eventDTO.setDescription(updateDTO.getDescription());
        eventDTO.setStartDate(updateDTO.getStartDate());
        eventDTO.setEndDate(updateDTO.getEndDate());
        eventDTO.setStatus(updateDTO.getStatus() != null ? updateDTO.getStatus() : existingEvent.getStatus());
        eventDTO.setBranchId(updateDTO.getBranchId());
        eventDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingEvent.getIsActive());
        
        eventRepository.update(eventDTO, userId);
        return getEventById(id);
    }
    
    @Transactional
    public void deleteEvent(Long id, Long userId) {
        // Check if event exists
        getEventById(id);
        
        // Perform soft delete
        eventRepository.delete(id, userId);
    }
}

