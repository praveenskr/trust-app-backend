package com.trustapp.dto;

import com.trustapp.dto.response.PageResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTransactionsDTO {
    private EventInfo event;
    private PageResponseDTO<DonationTransactionDTO> donations;
    private PageResponseDTO<ExpenseTransactionDTO> expenses;
    private PageResponseDTO<VoucherTransactionDTO> vouchers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfo {
        private Long id;
        private String code;
        private String name;
    }
}

