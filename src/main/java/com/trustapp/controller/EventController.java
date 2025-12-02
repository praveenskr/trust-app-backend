package com.trustapp.controller;

import com.trustapp.dto.EventCreateDTO;
import com.trustapp.dto.EventDashboardDTO;
import com.trustapp.dto.EventDTO;
import com.trustapp.dto.EventDropdownDTO;
import com.trustapp.dto.EventStatisticsDTO;
import com.trustapp.dto.EventStatusUpdateDTO;
import com.trustapp.dto.EventTransactionsDTO;
import com.trustapp.dto.EventUpdateDTO;
import com.trustapp.dto.UpcomingEventDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/master/events")
public class EventController {
    
    private final EventService eventService;
    
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<EventDTO>>> getAllEvents(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "startDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        PageResponseDTO<EventDTO> pageResponse = eventService.getAllEvents(
            branchId, status, includeInactive, fromDate, toDate, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable Long id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(
            @Valid @RequestBody EventCreateDTO createDTO,
            @RequestParam(required = false) Long createdBy) {
        EventDTO created = eventService.createEvent(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Event created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO updateDTO,
            @RequestParam(required = false) Long updatedBy) {
        EventDTO updated = eventService.updateEvent(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", updated));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EventDTO>> updateEventStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventStatusUpdateDTO statusUpdateDTO) {
        EventDTO updated = eventService.updateEventStatus(id, statusUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success("Event status updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(
            @PathVariable Long id,
            @RequestParam(required = false) Long deletedBy) {
        eventService.deleteEvent(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }
    
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<EventStatisticsDTO>> getEventStatistics(@PathVariable Long id) {
        EventStatisticsDTO statistics = eventService.getEventStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    
    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<EventTransactionsDTO>> getEventTransactions(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "ALL") String transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        EventTransactionsDTO transactions = eventService.getEventTransactions(
            id, transactionType, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<UpcomingEventDTO>>> getUpcomingEvents(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false, defaultValue = "30") Integer daysAhead,
            @RequestParam(required = false, defaultValue = "true") Boolean includeActive) {
        List<UpcomingEventDTO> events = eventService.getUpcomingEvents(branchId, daysAhead, includeActive);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
    
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<EventDropdownDTO>>> getAllEventsForDropdown(
            @RequestParam(required = false) Long branchId) {
        List<EventDropdownDTO> events = eventService.getAllEventsForDropdown(branchId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<EventDashboardDTO>> getEventDashboard(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Integer year) {
        EventDashboardDTO dashboard = eventService.getEventDashboard(branchId, year);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}

