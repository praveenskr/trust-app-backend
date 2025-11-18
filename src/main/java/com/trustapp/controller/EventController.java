package com.trustapp.controller;

import com.trustapp.dto.EventCreateDTO;
import com.trustapp.dto.EventDTO;
import com.trustapp.dto.EventUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/events")
public class EventController {
    
    private final EventService eventService;
    
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventDTO>>> getAllEvents(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<EventDTO> events = eventService.getAllEvents(branchId, status, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable Long id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(
            @Valid @RequestBody EventCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        EventDTO created = eventService.createEvent(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Event created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        EventDTO updated = eventService.updateEvent(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        eventService.deleteEvent(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }
}

