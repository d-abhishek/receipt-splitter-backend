package com.abhishek.receiptsplitterbackend.controller;

import com.abhishek.receiptsplitterbackend.service.SplitCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final SplitCalculationService splitCalculationService;

    public ReceiptController(SplitCalculationService splitCalculationService) {
        this.splitCalculationService = splitCalculationService;
    }

    @PostMapping("{id}/calculate")
    public ResponseEntity<String> calculateReceiptSplit(@PathVariable ("id") UUID receiptId){

        splitCalculationService.calculateSplit(receiptId);

        return ResponseEntity.ok("Receipt Split Calculated Successfully");
    }
}
