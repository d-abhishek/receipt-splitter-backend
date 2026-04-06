package com.abhishek.receiptsplitterbackend.controller;

import com.abhishek.receiptsplitterbackend.Dto.ReceiptRequest;
import com.abhishek.receiptsplitterbackend.entity.Receipt;
import com.abhishek.receiptsplitterbackend.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;

    @Autowired
    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /**
     * Add a new receipt with items.
     *
     * @param receiptRequest DTO containing receipt details and list of items
     * @return ResponseEntity with the created Receipt and its items
     */

    @PostMapping("/add")
    public ResponseEntity<String> addReceipt(@RequestBody ReceiptRequest receiptRequest, Authentication authentication) {
        try {
            String loggedInEmail = authentication.getName();
            Receipt savedReceipt = receiptService.addReceipt(receiptRequest, loggedInEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body("Receipt was saved successfully with ID: " + savedReceipt.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all receipts.
     */

    @GetMapping("/list")
    public ResponseEntity<List<Receipt>> getAllReceipts() {
        try {
            List<Receipt> receipts = receiptService.getAllReceipts();
            if (receipts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(receipts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific receipt by ID.
     */

    @GetMapping("/{receiptId}")
    public ResponseEntity<Receipt> getReceipt(@PathVariable UUID receiptId) {
        try {
            Receipt receipt = receiptService.getReceiptById(receiptId);
            return ResponseEntity.ok(receipt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing receipt by ID
     * @param receiptId contains UUID of the receipt to be updated
     * @param receiptRequest contains details of the receipt to be updated
     * @return ResponseEntity with the updated Receipt
     */

    @PutMapping("/{receiptId}")
    public ResponseEntity<Receipt> updateReceipt(@PathVariable UUID receiptId, @RequestBody ReceiptRequest receiptRequest, Authentication authentication) {
        try {
            String loggedInEmail = authentication.getName();
            Receipt updatedReceipt = receiptService.updateReceipt(receiptRequest, receiptId, loggedInEmail);
            return ResponseEntity.ok(updatedReceipt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete an existing receipt by ID
     * @param receiptId contains UUID of the receipt to be updated
     * @return Status OK
     */

    @DeleteMapping("/{receiptId}")
    public ResponseEntity<String> deleteReceipt(@PathVariable UUID receiptId) {
        try {
            receiptService.deleteReceipt(receiptId);
            return ResponseEntity.status(HttpStatus.OK).body("Receipt was deleted successfully.");
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
