package com.abhishek.receiptsplitterbackend.service.receipt;

import com.abhishek.receiptsplitterbackend.Dto.ReceiptRequest;
import org.springframework.stereotype.Service;

@Service
public class ReceiptValidationService {

    public void validateReceiptRequest(ReceiptRequest receiptRequest) {
        if (receiptRequest.getStoreName() == null || receiptRequest.getStoreName().trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }

        if (receiptRequest.getAmount() == null || receiptRequest.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Receipt amount must be greater than zero");
        }

        if (receiptRequest.getPaidBy() == null) {
            throw new IllegalArgumentException("paidBy (user ID) is required");
        }
    }
}
