package com.abhishek.receiptsplitterbackend.service;

import com.abhishek.receiptsplitterbackend.Dto.ReceiptRequest;
import com.abhishek.receiptsplitterbackend.entity.Group;
import com.abhishek.receiptsplitterbackend.entity.Receipt;
import com.abhishek.receiptsplitterbackend.entity.ReceiptItem;
import com.abhishek.receiptsplitterbackend.entity.User;
import com.abhishek.receiptsplitterbackend.repository.GroupRepository;
import com.abhishek.receiptsplitterbackend.repository.ReceiptRepository;
import com.abhishek.receiptsplitterbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository,  UserRepository userRepository, GroupRepository groupRepository) {
        this.receiptRepository = receiptRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
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
        if (receiptRequest.getStoreName() == null || receiptRequest.getStoreName().trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }

        if (receiptRequest.getAmount() == null || receiptRequest.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Receipt amount must be greater than zero");
        }

        if (receiptRequest.getPaidBy() == null) {
            throw new IllegalArgumentException("paidBy (user ID) is required");
        }

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

        // Create ReceiptItems from request
        List<ReceiptItem> items = new ArrayList<>();

        if (receiptRequest.getReceiptItems() != null && !receiptRequest.getReceiptItems().isEmpty()) {

            for (ReceiptRequest.ReceiptItemRequest receiptItemRequest : receiptRequest.getReceiptItems()) {

                ReceiptItem receiptItem = new ReceiptItem();
                receiptItem.setName(receiptItemRequest.getName());
                receiptItem.setQuantity(receiptItemRequest.getQuantity());
                receiptItem.setUnitPrice(receiptItemRequest.getUnitPrice());
                receiptItem.setFinalPrice(receiptItemRequest.getFinalPrice());
                receiptItem.setReceipt(receipt); // Set the relationship
                items.add(receiptItem);
            }
        }

        receipt.setItems(items);

        return receiptRepository.save(receipt);
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
        if (receiptRequest.getStoreName() != null && receiptRequest.getStoreName().trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }

        if (receiptRequest.getAmount() != null && receiptRequest.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Receipt amount must be greater than zero");
        }

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

        // Replace items
        if (receiptRequest.getReceiptItems() != null && !receiptRequest.getReceiptItems().isEmpty()) {
            List<ReceiptItem> newItems = new ArrayList<>();
            List<ReceiptItem> managedItems = receipt.getItems();

            for (ReceiptRequest.ReceiptItemRequest itemRequest : receiptRequest.getReceiptItems()) {
                ReceiptItem newItem = new ReceiptItem();
                newItem.setName(itemRequest.getName());
                newItem.setQuantity(itemRequest.getQuantity());
                newItem.setUnitPrice(itemRequest.getUnitPrice());
                newItem.setFinalPrice(itemRequest.getFinalPrice());
                newItem.setReceipt(receipt); // Link to parent
                newItems.add(newItem);
            }

            // Clear old items (orphanRemoval will delete from DB)
            managedItems.clear();

            // Add new items
            managedItems.addAll(newItems);
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
