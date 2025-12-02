package com.trustapp.service;

import com.trustapp.dto.DonationTransactionDTO;
import com.trustapp.dto.EventCreateDTO;
import com.trustapp.dto.EventDashboardDTO;
import com.trustapp.dto.EventDTO;
import com.trustapp.dto.EventDTO.BranchInfo;
import com.trustapp.dto.EventDropdownDTO;
import com.trustapp.dto.EventStatisticsDTO;
import com.trustapp.dto.EventStatusUpdateDTO;
import com.trustapp.dto.EventTransactionsDTO;
import com.trustapp.dto.EventUpdateDTO;
import com.trustapp.dto.UpcomingEventDTO;
import com.trustapp.dto.UserDTO;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.UserRepository;
import com.trustapp.repository.BranchRepository;
import com.trustapp.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {
    
    private final EventRepository eventRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    
    public EventService(
            EventRepository eventRepository,
            BranchRepository branchRepository,
            UserRepository userRepository,
            AuthenticationService authenticationService
    ) {
        this.eventRepository = eventRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }
    
    public PageResponseDTO<EventDTO> getAllEvents(Long branchId, String status, Boolean includeInactive,
                                                   LocalDate fromDate, LocalDate toDate, String search,
                                                   Integer page, Integer size, String sortBy, String sortDir) {
        // Set defaults
        boolean includeInactiveFlag = includeInactive != null && includeInactive;
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        String sortField = sortBy != null ? sortBy : "startDate";
        String sortDirection = sortDir != null ? sortDir : "DESC";
        
        // Get events
        List<EventDTO> events = eventRepository.findAll(
            branchId, status, includeInactiveFlag, fromDate, toDate, search,
            pageNum, pageSize, sortField, sortDirection
        );
        
        // Get total count
        long totalElements = eventRepository.count(
            branchId, status, includeInactiveFlag, fromDate, toDate, search
        );
        
        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = pageNum == 0;
        boolean isLast = pageNum >= totalPages - 1;
        int numberOfElements = events.size();
        
        PageResponseDTO<EventDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(events);
        pageResponse.setTotalElements(totalElements);
        pageResponse.setTotalPages(totalPages);
        pageResponse.setSize(pageSize);
        pageResponse.setNumber(pageNum);
        pageResponse.setFirst(isFirst);
        pageResponse.setLast(isLast);
        pageResponse.setNumberOfElements(numberOfElements);
        
        return pageResponse;
    }
    
    public EventDTO getEventById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public List<UpcomingEventDTO> getUpcomingEvents(Long branchId, Integer daysAhead, Boolean includeActive) {
        int effectiveDaysAhead = (daysAhead != null && daysAhead > 0) ? daysAhead : 30;
        boolean includeActiveFlag = includeActive == null || includeActive;
        return eventRepository.findUpcomingEvents(branchId, effectiveDaysAhead, includeActiveFlag);
    }
    
    @Transactional
    public EventDTO createEvent(EventCreateDTO createDTO, Long createdBy) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        
        // Use provided createdBy if specified, otherwise use authenticated user
        Long creatorId = createdBy != null ? createdBy : currentUserId;
        
        // Validate user exists
        userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));
        
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
        if (createDTO.getBranchId() != null) {
            BranchInfo branch = new BranchInfo();
            branch.setId(createDTO.getBranchId());
            eventDTO.setBranch(branch);
        }
        eventDTO.setIsActive(createDTO.getIsActive());
        
        Long id = eventRepository.save(eventDTO, creatorId);
        return getEventById(id);
    }
    
    @Transactional
    public EventDTO updateEvent(Long id, EventUpdateDTO updateDTO, Long updatedBy) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();

        // Use provided updatedBy if specified, otherwise use authenticated user
        Long updaterId = updatedBy != null ? updatedBy : currentUserId;

        // Validate user exists
        userRepository.findById(updaterId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + updaterId));

        // Check if event exists
        EventDTO existingEvent = getEventById(id);

        // Validate branch exists if branchId is provided
        if (updateDTO.getBranchId() != null) {
            branchRepository.findById(updateDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + updateDTO.getBranchId()));
        }

        // Determine effective dates (existing if not provided)
        LocalDate effectiveStartDate = updateDTO.getStartDate() != null
            ? updateDTO.getStartDate()
            : existingEvent.getStartDate();
        LocalDate effectiveEndDate = updateDTO.getEndDate() != null
            ? updateDTO.getEndDate()
            : existingEvent.getEndDate();

        // Validate date range: endDate must be after or equal to startDate
        if (effectiveEndDate != null && effectiveStartDate != null) {
            if (effectiveEndDate.isBefore(effectiveStartDate)) {
                throw new ValidationException("End date must be after or equal to start date");
            }
        }

        // Determine effective status (new status if provided, otherwise keep existing)
        String effectiveStatus = updateDTO.getStatus() != null ? updateDTO.getStatus() : existingEvent.getStatus();
        String currentStatus = existingEvent.getStatus();

        // Validate status transition if status is being changed
        if (updateDTO.getStatus() != null && !effectiveStatus.equals(currentStatus)) {
            validateStatusTransition(currentStatus, effectiveStatus, effectiveStartDate, effectiveEndDate);
        }

        // Check for associated transactions if trying to cancel event
        if ("CANCELLED".equals(effectiveStatus) && !"CANCELLED".equals(currentStatus)) {
            if (eventRepository.hasAssociatedDonations(id)) {
                throw new ValidationException("Cannot update event status to CANCELLED. Event has associated donations. Please remove associations first.");
            }
            // if (eventRepository.hasAssociatedExpenses(id)) {
            //     throw new ValidationException("Cannot update event status to CANCELLED. Event has associated expenses. Please remove associations first.");
            // }
            // if (eventRepository.hasAssociatedVouchers(id)) {
            //     throw new ValidationException("Cannot update event status to CANCELLED. Event has associated vouchers. Please remove associations first.");
            // }
        }

        // Convert UpdateDTO to DTO for updating (only override when provided)
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(id);
        eventDTO.setCode(existingEvent.getCode()); // Code is not updatable
        eventDTO.setName(updateDTO.getName() != null ? updateDTO.getName() : existingEvent.getName());
        eventDTO.setDescription(updateDTO.getDescription() != null ? updateDTO.getDescription() : existingEvent.getDescription());
        eventDTO.setStartDate(effectiveStartDate);
        eventDTO.setEndDate(effectiveEndDate);
        eventDTO.setStatus(effectiveStatus);

        Long targetBranchId = updateDTO.getBranchId() != null
            ? updateDTO.getBranchId()
            : (existingEvent.getBranch() != null ? existingEvent.getBranch().getId() : null);
        if (targetBranchId != null) {
            BranchInfo branch = new BranchInfo();
            branch.setId(targetBranchId);
            eventDTO.setBranch(branch);
        }

        eventDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingEvent.getIsActive());

        eventRepository.update(eventDTO, updaterId);
        return getEventById(id);
    }
    
    @Transactional
    public EventDTO updateEventStatus(Long id, EventStatusUpdateDTO statusUpdateDTO) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        
        // Check if event exists and get current status
        EventDTO existingEvent = getEventById(id);
        String currentStatus = existingEvent.getStatus();
        String newStatus = statusUpdateDTO.getStatus();
        
        // If status is not changing, return the event as is with previousStatus set
        if (currentStatus.equals(newStatus)) {
            existingEvent.setPreviousStatus(currentStatus);
            return existingEvent;
        }
        
        // Validate status transition
        validateStatusTransition(currentStatus, newStatus, 
                                existingEvent.getStartDate(), existingEvent.getEndDate());
        
        // Update status in database
        eventRepository.updateStatus(id, newStatus, currentUserId);
        
        // Return updated event with previousStatus
        EventDTO updatedEvent = getEventById(id);
        updatedEvent.setPreviousStatus(currentStatus);
        return updatedEvent;
    }
    
    @Transactional
    public void deleteEvent(Long id, Long deletedBy) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        
        // Use provided deletedBy if specified, otherwise use authenticated user
        Long deleterId = deletedBy != null ? deletedBy : currentUserId;
        
        // Validate user exists
        userRepository.findById(deleterId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + deleterId));
        
        // Check if event exists
        getEventById(id);
        
        // Check for associated transactions before deleting
        if (eventRepository.hasAssociatedTransactions(id)) {
            StringBuilder errorMessage = new StringBuilder("Cannot delete event. Event has associated ");
            List<String> associations = new java.util.ArrayList<>();
            
            if (eventRepository.hasAssociatedDonations(id)) {
                associations.add("donations");
            }
            // if (eventRepository.hasAssociatedExpenses(id)) {
            //     associations.add("expenses");
            // }
            // if (eventRepository.hasAssociatedVouchers(id)) {
            //     associations.add("vouchers");
            // }
            
            errorMessage.append(String.join(", ", associations));
            errorMessage.append(". Please remove associations first.");
            
            throw new ValidationException(errorMessage.toString());
        }
        
        // Perform soft delete
        eventRepository.delete(id, deleterId);
    }
    
    public List<EventDropdownDTO> getAllEventsForDropdown(Long branchId) {
        return eventRepository.findAllForDropdown(branchId);
    }
    
    /**
     * Validates event status transitions according to lifecycle rules.
     * 
     * Status Transition Rules:
     * - PLANNED → ACTIVE: Allowed when current date >= start_date
     * - PLANNED → COMPLETED: Allowed when current date >= end_date
     * - PLANNED → CANCELLED: Always allowed
     * - ACTIVE → COMPLETED: Allowed when current date >= end_date
     * - ACTIVE → CANCELLED: Always allowed
     * - COMPLETED → ACTIVE: Not allowed (one-way transition)
     * - COMPLETED → PLANNED: Not allowed
     * - CANCELLED → Any status: Not allowed (cancelled events cannot be reactivated)
     */
    private void validateStatusTransition(String fromStatus, String toStatus, 
                                          LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();
        
        // CANCELLED events cannot be reactivated
        if ("CANCELLED".equals(fromStatus)) {
            throw new ValidationException(
                "Invalid status transition from CANCELLED to " + toStatus + ". Cancelled events cannot be reactivated.");
        }
        
        // COMPLETED events cannot be changed to ACTIVE or PLANNED
        if ("COMPLETED".equals(fromStatus) && ("ACTIVE".equals(toStatus) || "PLANNED".equals(toStatus))) {
            throw new ValidationException(
                "Invalid status transition from COMPLETED to " + toStatus + ". Completed events cannot be reactivated.");
        }
        
        // Validate specific transitions
        switch (fromStatus) {
            case "PLANNED":
                if ("ACTIVE".equals(toStatus)) {
                    if (startDate != null && currentDate.isBefore(startDate)) {
                        throw new ValidationException(
                            "Invalid status transition from PLANNED to ACTIVE. Current date must be after or equal to start date.");
                    }
                } else if ("COMPLETED".equals(toStatus)) {
                    if (endDate != null && currentDate.isBefore(endDate)) {
                        throw new ValidationException(
                            "Invalid status transition from PLANNED to COMPLETED. Current date must be after or equal to end date.");
                    }
                }
                // PLANNED → CANCELLED is always allowed
                break;
                
            case "ACTIVE":
                if ("COMPLETED".equals(toStatus)) {
                    if (endDate != null && currentDate.isBefore(endDate)) {
                        throw new ValidationException(
                            "Invalid status transition from ACTIVE to COMPLETED. Current date must be after or equal to end date.");
                    }
                }
                // ACTIVE → CANCELLED is always allowed
                // ACTIVE → PLANNED is not allowed (handled by general rule)
                if ("PLANNED".equals(toStatus)) {
                    throw new ValidationException(
                        "Invalid status transition from ACTIVE to PLANNED. Active events cannot be changed back to planned.");
                }
                break;
                
            case "COMPLETED":
                // COMPLETED → Any status (except itself) is not allowed
                // This is already handled above, but keeping for clarity
                break;
                
            case "CANCELLED":
                // CANCELLED → Any status is not allowed
                // This is already handled above
                break;
        }
    }
    
    public EventStatisticsDTO getEventStatistics(Long id) {
        // Check if event exists
        EventDTO event = getEventById(id);
        
        // Create event info
        EventStatisticsDTO.EventInfo eventInfo = new EventStatisticsDTO.EventInfo();
        eventInfo.setId(event.getId());
        eventInfo.setCode(event.getCode());
        eventInfo.setName(event.getName());
        eventInfo.setStartDate(event.getStartDate());
        eventInfo.setEndDate(event.getEndDate());
        eventInfo.setStatus(event.getStatus());
        
        // Get statistics
        EventStatisticsDTO.DonationStatistics donationStats = eventRepository.getDonationStatistics(id);
        // EventStatisticsDTO.ExpenseStatistics expenseStats = eventRepository.getExpenseStatistics(id);
        // EventStatisticsDTO.VoucherStatistics voucherStats = eventRepository.getVoucherStatistics(id);
        
        // Set default/empty values for expenses and vouchers (tables not implemented yet)
        EventStatisticsDTO.ExpenseStatistics expenseStats = new EventStatisticsDTO.ExpenseStatistics();
        expenseStats.setTotalCount(0L);
        expenseStats.setTotalAmount(BigDecimal.ZERO);
        expenseStats.setAverageAmount(BigDecimal.ZERO);
        expenseStats.setByCategory(new java.util.ArrayList<>());
        
        EventStatisticsDTO.VoucherStatistics voucherStats = new EventStatisticsDTO.VoucherStatistics();
        voucherStats.setTotalCount(0L);
        voucherStats.setTotalAmount(BigDecimal.ZERO);
        voucherStats.setAverageAmount(BigDecimal.ZERO);
        
        // Calculate financial summary
        BigDecimal totalIncome = donationStats.getTotalAmount() != null ? donationStats.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO; // expenseStats.getTotalAmount() != null ? expenseStats.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal voucherAmount = BigDecimal.ZERO; // voucherStats.getTotalAmount() != null ? voucherStats.getTotalAmount() : BigDecimal.ZERO;
        
        // Vouchers are also expenses
        BigDecimal totalAllExpenses = totalExpenses.add(voucherAmount);
        BigDecimal netAmount = totalIncome.subtract(totalAllExpenses);
        
        BigDecimal profitMargin = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = netAmount.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        EventStatisticsDTO.FinancialSummary financialSummary = new EventStatisticsDTO.FinancialSummary();
        financialSummary.setTotalIncome(totalIncome);
        financialSummary.setTotalExpenses(totalAllExpenses);
        financialSummary.setNetAmount(netAmount);
        financialSummary.setProfitMargin(profitMargin);
        
        // Calculate timeline
        EventStatisticsDTO.Timeline timeline = calculateTimeline(event.getStartDate(), event.getEndDate());
        
        // Build response
        EventStatisticsDTO statistics = new EventStatisticsDTO();
        statistics.setEvent(eventInfo);
        statistics.setDonations(donationStats);
        statistics.setExpenses(expenseStats);
        statistics.setVouchers(voucherStats);
        statistics.setFinancialSummary(financialSummary);
        statistics.setTimeline(timeline);
        
        return statistics;
    }
    
    private EventStatisticsDTO.Timeline calculateTimeline(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();
        EventStatisticsDTO.Timeline timeline = new EventStatisticsDTO.Timeline();
        
        if (startDate == null) {
            timeline.setDaysRemaining(0);
            timeline.setDaysElapsed(0);
            timeline.setTotalDays(0);
            timeline.setCompletionPercentage(BigDecimal.ZERO);
            return timeline;
        }
        
        // Calculate total days
        int totalDays = 1; // At least 1 day
        if (endDate != null && endDate.isAfter(startDate)) {
            totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        timeline.setTotalDays(totalDays);
        
        // Calculate days elapsed
        int daysElapsed = 0;
        if (currentDate.isAfter(startDate) || currentDate.isEqual(startDate)) {
            if (endDate != null && currentDate.isAfter(endDate)) {
                daysElapsed = totalDays; // Event has ended
            } else {
                daysElapsed = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, currentDate) + 1;
                if (daysElapsed > totalDays) {
                    daysElapsed = totalDays;
                }
            }
        }
        timeline.setDaysElapsed(daysElapsed);
        
        // Calculate days remaining
        int daysRemaining = totalDays - daysElapsed;
        if (daysRemaining < 0) {
            daysRemaining = 0;
        }
        timeline.setDaysRemaining(daysRemaining);
        
        // Calculate completion percentage
        BigDecimal completionPercentage = BigDecimal.ZERO;
        if (totalDays > 0) {
            completionPercentage = BigDecimal.valueOf(daysElapsed)
                .divide(BigDecimal.valueOf(totalDays), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        timeline.setCompletionPercentage(completionPercentage);
        
        return timeline;
    }
    
    public EventTransactionsDTO getEventTransactions(Long id, String transactionType, 
                                                      LocalDate fromDate, LocalDate toDate,
                                                      Integer page, Integer size) {
        // Check if event exists
        EventDTO event = getEventById(id);
        
        // Set defaults
        String type = transactionType != null ? transactionType : "ALL";
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        
        // Create event info
        EventTransactionsDTO.EventInfo eventInfo = new EventTransactionsDTO.EventInfo();
        eventInfo.setId(event.getId());
        eventInfo.setCode(event.getCode());
        eventInfo.setName(event.getName());
        
        // Initialize response
        EventTransactionsDTO response = new EventTransactionsDTO();
        response.setEvent(eventInfo);
        
        // Fetch transactions based on type
        if ("ALL".equals(type) || "DONATIONS".equals(type)) {
            List<DonationTransactionDTO> donations = eventRepository.getDonationsByEventId(
                id, fromDate, toDate, pageNum, pageSize);
            long totalDonations = eventRepository.countDonationsByEventId(id, fromDate, toDate);
            
            PageResponseDTO<DonationTransactionDTO> donationsPage = createPageResponse(
                donations, totalDonations, pageNum, pageSize);
            response.setDonations(donationsPage);
        } else {
            response.setDonations(createEmptyPageResponse());
        }
        
        // if ("ALL".equals(type) || "EXPENSES".equals(type)) {
        //     List<ExpenseTransactionDTO> expenses = eventRepository.getExpensesByEventId(
        //         id, fromDate, toDate, pageNum, pageSize);
        //     long totalExpenses = eventRepository.countExpensesByEventId(id, fromDate, toDate);
            
        //     PageResponseDTO<ExpenseTransactionDTO> expensesPage = createPageResponse(
        //         expenses, totalExpenses, pageNum, pageSize);
        //     response.setExpenses(expensesPage);
        // } else {
        //     response.setExpenses(createEmptyPageResponse());
        // }
        
        // if ("ALL".equals(type) || "VOUCHERS".equals(type)) {
        //     List<VoucherTransactionDTO> vouchers = eventRepository.getVouchersByEventId(
        //         id, fromDate, toDate, pageNum, pageSize);
        //     long totalVouchers = eventRepository.countVouchersByEventId(id, fromDate, toDate);
            
        //     PageResponseDTO<VoucherTransactionDTO> vouchersPage = createPageResponse(
        //         vouchers, totalVouchers, pageNum, pageSize);
        //     response.setVouchers(vouchersPage);
        // } else {
        //     response.setVouchers(createEmptyPageResponse());
        // }
        
        return response;
    }
    
    private <T> PageResponseDTO<T> createPageResponse(List<T> content, long totalElements, 
                                                      int page, int size) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isFirst = page == 0;
        boolean isLast = page >= totalPages - 1;
        int numberOfElements = content.size();
        
        PageResponseDTO<T> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(content);
        pageResponse.setTotalElements(totalElements);
        pageResponse.setTotalPages(totalPages);
        pageResponse.setSize(size);
        pageResponse.setNumber(page);
        pageResponse.setFirst(isFirst);
        pageResponse.setLast(isLast);
        pageResponse.setNumberOfElements(numberOfElements);
        
        return pageResponse;
    }
    
    private <T> PageResponseDTO<T> createEmptyPageResponse() {
        PageResponseDTO<T> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(List.of());
        pageResponse.setTotalElements(0L);
        pageResponse.setTotalPages(0);
        pageResponse.setSize(20);
        pageResponse.setNumber(0);
        pageResponse.setFirst(true);
        pageResponse.setLast(true);
        pageResponse.setNumberOfElements(0);
        return pageResponse;
    }
    
    public EventDashboardDTO getEventDashboard(Long branchId, Integer year) {
        // Set default year to current year if not provided
        int effectiveYear = (year != null && year > 0) ? year : LocalDate.now().getYear();
        
        // Build dashboard DTO
        EventDashboardDTO dashboard = new EventDashboardDTO();
        
        // Get summary
        dashboard.setSummary(eventRepository.getEventSummary(branchId, effectiveYear));
        
        // Get upcoming events
        dashboard.setUpcomingEvents(eventRepository.getUpcomingEventsSummary(branchId));
        
        // Get active events
        dashboard.setActiveEvents(eventRepository.getActiveEventsSummary(branchId));
        
        // Get financial overview
        dashboard.setFinancialOverview(eventRepository.getFinancialOverview(branchId, effectiveYear));
        
        // Get monthly breakdown
        dashboard.setMonthlyBreakdown(eventRepository.getMonthlyBreakdown(branchId, effectiveYear));
        
        return dashboard;
    }
}

