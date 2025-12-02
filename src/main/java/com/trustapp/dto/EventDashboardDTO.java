package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDashboardDTO {
    private Summary summary;
    private List<UpcomingEventSummary> upcomingEvents;
    private List<ActiveEventSummary> activeEvents;
    private FinancialOverview financialOverview;
    private List<MonthlyBreakdown> monthlyBreakdown;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long totalEvents;
        private Long plannedEvents;
        private Long activeEvents;
        private Long completedEvents;
        private Long cancelledEvents;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingEventSummary {
        private Long id;
        private String code;
        private String name;
        private LocalDate startDate;
        private String status;
        private Long daysUntilStart;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveEventSummary {
        private Long id;
        private String code;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private BigDecimal totalDonations;
        private BigDecimal totalExpenses;
        private BigDecimal netAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialOverview {
        private BigDecimal totalEventIncome;
        private BigDecimal totalEventExpenses;
        private BigDecimal netEventProfit;
        private BigDecimal averageEventProfit;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyBreakdown {
        private String month;
        private Long eventCount;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
    }
}

