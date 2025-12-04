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
public class EventStatisticsDTO {
    private EventInfo event;
    private DonationStatistics donations;
    private ExpenseStatistics expenses;
    private VoucherStatistics vouchers;
    private FinancialSummary financialSummary;
    private Timeline timeline;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfo {
        private Long id;
        private String code;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DonationStatistics {
        private Long totalCount;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private List<PaymentModeStat> byPaymentMode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentModeStat {
        private String paymentMode;
        private Long count;
        private BigDecimal totalAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseStatistics {
        private Long totalCount;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private List<CategoryStat> byCategory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {
        private String category;
        private Long count;
        private BigDecimal totalAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherStatistics {
        private Long totalCount;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netAmount;
        private BigDecimal profitMargin;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Timeline {
        private Integer daysRemaining;
        private Integer daysElapsed;
        private Integer totalDays;
        private BigDecimal completionPercentage;
    }
}

