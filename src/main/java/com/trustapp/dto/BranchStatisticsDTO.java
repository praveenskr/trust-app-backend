package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchStatisticsDTO {
    private BranchInfo branch;
    private DonationStatistics donations;
    private ExpenseStatistics expenses;
    private VoucherStatistics vouchers;
    private EventStatistics events;
    private UserStatistics users;
    private FinancialSummary financialSummary;
    private InterBranchTransferStatistics interBranchTransfers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchInfo {
        private Long id;
        private String code;
        private String name;
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
    public static class EventStatistics {
        private Long totalCount;
        private Long activeCount;
        private Long completedCount;
        private Long plannedCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        private Long totalCount;
        private Long activeCount;
        private Long inactiveCount;
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
    public static class InterBranchTransferStatistics {
        private BigDecimal totalIncoming;
        private BigDecimal totalOutgoing;
        private BigDecimal netTransfer;
    }
}

