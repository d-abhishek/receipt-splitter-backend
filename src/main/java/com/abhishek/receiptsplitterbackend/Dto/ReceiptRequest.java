package com.abhishek.receiptsplitterbackend.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ReceiptRequest {

    private String description;
    private String storeName;
    private BigDecimal amount;
    private String currency;
    private UUID paidBy;
    private UUID groupId;
    private LocalDate receiptDate;
    private List<ReceiptItemRequest> receiptItems;

    @Data
    public static class ReceiptItemRequest {

        private String name;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal finalPrice;
    }
}
