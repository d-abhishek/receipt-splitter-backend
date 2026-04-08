package com.abhishek.receiptsplitterbackend.service.receipt;

import com.abhishek.receiptsplitterbackend.Dto.ReceiptRequest;
import com.abhishek.receiptsplitterbackend.entity.*;
import com.abhishek.receiptsplitterbackend.repository.GroupRepository;
import com.abhishek.receiptsplitterbackend.repository.ReceiptRepository;
import com.abhishek.receiptsplitterbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ReceiptValidationService receiptValidationService;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository,  UserRepository userRepository, GroupRepository groupRepository,  ReceiptValidationService receiptValidationService) {
        this.receiptRepository = receiptRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.receiptValidationService = receiptValidationService;
    }

    /**
     * Adds a new receipt to the database.
     *
     * @param receiptRequest contains details of the receipt to be added
     * @return the saved Receipt entity
     * @throws IllegalArgumentException if the request data is invalid
     */

    public Receipt addReceipt(ReceiptRequest receiptRequest, String loggedInEmail) {

        // Validate input
        receiptValidationService.validateReceiptRequest(receiptRequest);

        // Fetch users
        User uploadedByUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found: " + loggedInEmail));

        User paidByUser = userRepository.findById(receiptRequest.getPaidBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + receiptRequest.getPaidBy()));

        // Fetch group if provided
        Group group = null;
        if (receiptRequest.getGroupId() != null) {
            group = groupRepository.findById(receiptRequest.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: " + receiptRequest.getGroupId()));
        }

        // Create Receipt entity
        Receipt receipt = new Receipt();
        receipt.setDescription(receiptRequest.getDescription());
        receipt.setStoreName(receiptRequest.getStoreName());
        receipt.setAmount(receiptRequest.getAmount());
        receipt.setCurrency(receiptRequest.getCurrency() != null ? receiptRequest.getCurrency() : "EUR");
        receipt.setUploadedBy(uploadedByUser);
        receipt.setPaidBy(paidByUser);
        receipt.setGroup(group);
        receipt.setReceiptDate(receiptRequest.getReceiptDate());
        receipt.setSplitType(receiptRequest.getSplitType());

        if (Objects.equals(receiptRequest.getSplitType(), "RECEIPT_LEVEL")) {

            List<ReceiptSplit> receiptSplits = new ArrayList<>();

            calculateReceiptLevelSplit(receiptRequest, receipt, receiptSplits);

            receipt.setSplits(receiptSplits);
        }

        // Create ReceiptItems from request
        List<ReceiptItem> items = new ArrayList<>();

        if (receiptRequest.getReceiptItems() != null && !receiptRequest.getReceiptItems().isEmpty()) {
            populateReceiptItems(receiptRequest, receipt, items);
        }

        receipt.setItems(items);

        return receiptRepository.save(receipt);
    }

    private void calculateReceiptLevelSplit(ReceiptRequest receiptRequest, Receipt receipt, List<ReceiptSplit> receiptSplits) {

        BigDecimal totalPercentage = BigDecimal.ZERO;

        for (ReceiptRequest.Split splitRequest : receiptRequest.getSplits()) {
            if (splitRequest.getUserId() == null || splitRequest.getPercentage() == null) {
                throw new IllegalArgumentException("Split userId and percentage are required");
            }

            User splitUser = userRepository.findById(splitRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Split user not found: " + splitRequest.getUserId()));

            BigDecimal amountOwed = receiptRequest.getAmount()
                    .multiply(splitRequest.getPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            ReceiptSplit receiptSplit = new ReceiptSplit();

            receiptSplit.setReceipt(receipt);
            receiptSplit.setUser(splitUser);
            receiptSplit.setAmountOwed(amountOwed);

            receiptSplits.add(receiptSplit);

            totalPercentage = totalPercentage.add(splitRequest.getPercentage());
        }

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Item split percentages must total 100 for receipt: " + receiptRequest.getStoreName() + " expense on " + receiptRequest.getStoreName());
        }
    }

    private void populateReceiptItems(ReceiptRequest receiptRequest, Receipt receipt, List<ReceiptItem> items) {
        for (ReceiptRequest.ReceiptItemRequest receiptItemRequest : receiptRequest.getReceiptItems()) {

            ReceiptItem receiptItem = new ReceiptItem();
            receiptItem.setName(receiptItemRequest.getName());
            receiptItem.setQuantity(receiptItemRequest.getQuantity());
            receiptItem.setUnitPrice(receiptItemRequest.getUnitPrice());
            receiptItem.setFinalPrice(receiptItemRequest.getFinalPrice());
            receiptItem.setReceipt(receipt); // Set the relationship

            if (Objects.equals(receiptRequest.getSplitType(), "ITEM_LEVEL")) {

                calculateItemLevelSplit(receiptItemRequest, receiptItem);
            }

            items.add(receiptItem);
        }
    }

    private void calculateItemLevelSplit(ReceiptRequest.ReceiptItemRequest receiptItemRequest, ReceiptItem receiptItem) {
        BigDecimal totalPercentage = BigDecimal.ZERO;
        List<ReceiptItemSplit> itemSplits = new ArrayList<>();

        for (ReceiptRequest.Split splitRequest : receiptItemRequest.getSplits()) {
            if (splitRequest.getUserId() == null || splitRequest.getPercentage() == null) {
                throw new IllegalArgumentException("Split userId and percentage are required");
            }

            User splitUser = userRepository.findById(splitRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Split user not found: " + splitRequest.getUserId()));

            BigDecimal amountOwed = receiptItemRequest.getFinalPrice()
                    .multiply(splitRequest.getPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            ReceiptItemSplit receiptItemSplit = new ReceiptItemSplit();

            receiptItemSplit.setItem(receiptItem);
            receiptItemSplit.setUser(splitUser);
            receiptItemSplit.setAmountOwed(amountOwed);

            itemSplits.add(receiptItemSplit);

            totalPercentage = totalPercentage.add(splitRequest.getPercentage());
        }

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Item split percentages must total 100 for item: " + receiptItemRequest.getName());
        }

        receiptItem.setSplits(itemSplits);
    }

    /**
     * Retrieves all receipts.
     */

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    /**
     * Retrieves a receipt by ID.
     * @param receiptId contains UUID of the receipt to be retrieved
     * @return the requested Receipt
     */

    public Receipt getReceiptById(UUID receiptId) {
        return receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptId));
    }

    /**
     * Update an existing receipt from the database.
     *
     * @param receiptRequest contains details of the receipt to be updated
     * @return the saved Receipt entity
     * @throws IllegalArgumentException if the request data is invalid
     */

    public Receipt updateReceipt(ReceiptRequest receiptRequest, UUID receiptId, String loggedInEmail) {

        // Fetch existing receipt
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptId));

        // Validate input
        receiptValidationService.validateReceiptRequest(receiptRequest);

        // Verify logged-in user is the one who uploaded the receipt
        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found: " + loggedInEmail));

        if (!receipt.getUploadedBy().getId().equals(loggedInUser.getId())) {
            throw new IllegalArgumentException("Only the original uploader can update this receipt");
        }

        // Update fields (only if provided)
        if (receiptRequest.getDescription() != null) {
            receipt.setDescription(receiptRequest.getDescription());
        }
        if (receiptRequest.getStoreName() != null) {
            receipt.setStoreName(receiptRequest.getStoreName());
        }
        if (receiptRequest.getAmount() != null) {
            receipt.setAmount(receiptRequest.getAmount());
        }
        if (receiptRequest.getCurrency() != null) {
            receipt.setCurrency(receiptRequest.getCurrency());
        }
        if (receiptRequest.getReceiptDate() != null) {
            receipt.setReceiptDate(receiptRequest.getReceiptDate());
        }

        // Update paidBy if provided
        if (receiptRequest.getPaidBy() != null) {
            User paidByUser = userRepository.findById(receiptRequest.getPaidBy())
                    .orElseThrow(() -> new IllegalArgumentException("Paid-by user not found: " + receiptRequest.getPaidBy()));
            receipt.setPaidBy(paidByUser);
        }

        // Update group if provided
        if (receiptRequest.getGroupId() != null) {
            Group group = groupRepository.findById(receiptRequest.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: " + receiptRequest.getGroupId()));
            receipt.setGroup(group);
        }

        if (receiptRequest.getSplitType() != null) {
            if (receiptRequest.getSplitType().equals("RECEIPT_LEVEL") && receipt.getSplitType().equals("ITEM_LEVEL")) {
                // Clear item-level splits
                for (ReceiptItem item : receipt.getItems()) {
                    item.getSplits().clear();
                }
                receipt.getSplits().clear();

            } else if (receiptRequest.getSplitType().equals("ITEM_LEVEL") && receipt.getSplitType().equals("RECEIPT_LEVEL")) {
                // Clear receipt-level splits
                receipt.getSplits().clear();
            }

            receipt.setSplitType(receiptRequest.getSplitType());

            if (Objects.equals(receiptRequest.getSplitType(), "RECEIPT_LEVEL")) {

                for (ReceiptItem item : receipt.getItems()) {
                    item.getSplits().clear();
                }

                List<ReceiptSplit> managedSplits = receipt.getSplits();
                managedSplits.clear(); // orphanRemoval deletes old rows

                calculateReceiptLevelSplit(receiptRequest, receipt, managedSplits);
            }

        }

        // Replace items
        if (receiptRequest.getReceiptItems() != null && !receiptRequest.getReceiptItems().isEmpty()) {
            List<ReceiptItem> receiptItems = new ArrayList<>();
            List<ReceiptItem> managedItems = receipt.getItems();

            populateReceiptItems(receiptRequest, receipt, receiptItems);

            // Clear old items (orphanRemoval will delete from DB)
            managedItems.clear();

            // Add new items
            managedItems.addAll(receiptItems);
        }

        return receiptRepository.save(receipt);
    }

    /**
     * Delete an existing receipt from the database.
     *
     * @param receiptId contains UUID of the receipt to be deleted
     * @throws IllegalArgumentException if the request data is invalid
     */

    public void deleteReceipt(UUID receiptId) {
        try {
            Receipt receipt = getReceiptById(receiptId);
            receiptRepository.delete(receipt);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Receipt not found with ID: " + receiptId);
        }
    }


}
