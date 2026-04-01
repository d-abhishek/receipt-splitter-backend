package com.abhishek.receiptsplitterbackend.service;

import com.abhishek.receiptsplitterbackend.entity.*;
import com.abhishek.receiptsplitterbackend.repository.GroupItemSplitRuleRepository;
import com.abhishek.receiptsplitterbackend.repository.ReceiptItemRepository;
import com.abhishek.receiptsplitterbackend.repository.ReceiptItemSplitRepository;
import com.abhishek.receiptsplitterbackend.repository.ReceiptRepository;
import com.abhishek.receiptsplitterbackend.service.calculator.ItemSplit;
import com.abhishek.receiptsplitterbackend.service.calculator.ItemSplitResult;
import com.abhishek.receiptsplitterbackend.service.calculator.SplitCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SplitCalculationService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final GroupItemSplitRuleRepository groupItemSplitRuleRepository;
    private final ReceiptItemSplitRepository receiptItemSplitRepository;

    private final SplitCalculator splitCalculator;

    public SplitCalculationService(
            ReceiptRepository receiptRepository,
            ReceiptItemRepository receiptItemRepository,
            GroupItemSplitRuleRepository groupItemSplitRuleRepository,
            ReceiptItemSplitRepository receiptItemSplitRepository) {
        this.receiptRepository = receiptRepository;
        this.receiptItemRepository = receiptItemRepository;
        this.groupItemSplitRuleRepository = groupItemSplitRuleRepository;
        this.receiptItemSplitRepository = receiptItemSplitRepository;
        this.splitCalculator = new SplitCalculator();
    }

    @Transactional
    public void calculateSplit(UUID receiptID) {

        // Fetch Receipt
        Receipt receipt = receiptRepository.findById(receiptID).orElseThrow(() -> new RuntimeException("Receipt not found"));

        // Fetch Receipt Items
        List<ReceiptItem> items = receiptItemRepository.findByReceiptId(receiptID);

        // Get Participants
        List<User> participants = getParticipants(receipt);

        // Fetch Rules (if exists)
        List<GroupItemSplitRule> rules = receipt.getGroup() != null ? groupItemSplitRuleRepository.findByGroupId(receipt.getGroup().getId()) : Collections.emptyList();

        // Delete Old Splits
        receiptItemSplitRepository.deleteByItemReceiptId(receiptID);

        if (!items.isEmpty()) {

            // Calculate New Splits
            List<ItemSplit> splits = splitCalculator.calculate(items, participants, rules);

            // Handle Rounding-Off
            handleRounding(splits, items, receipt);

            // Save the splits
            saveItemSplits(splits, receipt);
        }
        else {
            // TODO: Future Implementation for Direct Splits
            throw new RuntimeException("Receipt-level split not implemented yet");
        }
    }

    // --------------------------
    // Helper: Get Participants
    // --------------------------
    private List<User> getParticipants(Receipt receipt) {

        if (receipt.getGroup() != null) {
            return new ArrayList<>(receipt.getGroup().getMembers());
        }

        // TODO: Add a feature to setup participants directly if group does not exist
        // For now, fallback: only the uploader (individual split)
        return List.of(receipt.getUploadedBy());
    }

    // --------------------------
    // Helper: Save splits
    // --------------------------
    private void saveItemSplits(List<ItemSplit> itemSplits, Receipt receipt) {

        List<ReceiptItemSplit> entities = new ArrayList<>();

        for (ItemSplit itemSplit : itemSplits) {

            ReceiptItem item = receiptItemRepository.findById(itemSplit.getItemId())
                    .orElseThrow(() -> new RuntimeException("ReceiptItem not found"));

            for (ItemSplitResult itemSplitResult : itemSplit.getSplits()) {

                ReceiptItemSplit entity = new ReceiptItemSplit();

                entity.setItem(item);
                entity.setUser(itemSplitResult.getUser());
                entity.setAmountOwed(itemSplitResult.getAmount());

                entities.add(entity);
            }
        }

        receiptItemSplitRepository.saveAll(entities);
    }

    // --------------------------
    // Helper: Rounding handling
    // --------------------------
    private void handleRounding(List<ItemSplit> itemSplits, List<ReceiptItem> items, Receipt receipt) {

        BigDecimal expectedTotal = items.stream().map(ReceiptItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualTotal = itemSplits.stream().flatMap(itemSplit -> itemSplit.getSplits().stream()).map(ItemSplitResult::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal difference = expectedTotal.subtract(actualTotal);

        if  (difference.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        User payer = receipt.getUploadedBy() != null ? receipt.getUploadedBy() : itemSplits.getFirst().getSplits().getFirst().getUser();

        for  (ItemSplit itemSplit : itemSplits) {
            for (ItemSplitResult itemSplitResult : itemSplit.getSplits()) {

                if (itemSplitResult.getUser().getId().equals(payer.getId())) {

                    BigDecimal updated = itemSplitResult.getAmount().add(difference);
                    itemSplitResult = new ItemSplitResult(itemSplitResult.getUser(), updated);

                    // Replace in list
                    itemSplit.getSplits().removeIf(r -> r.getUser().getId().equals(payer.getId()));
                    itemSplit.getSplits().add(itemSplitResult);
                    return;
                }
            }
        }
    }
}
