package com.abhishek.receiptsplitterbackend.service.calculator;

import com.abhishek.receiptsplitterbackend.entity.GroupItemSplitRule;
import com.abhishek.receiptsplitterbackend.entity.ReceiptItem;
import com.abhishek.receiptsplitterbackend.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SplitCalculator {

    public List<ItemSplit> calculate(
            List<ReceiptItem> items,
            List<User> participants,
            List<GroupItemSplitRule> rules){

        List<ItemSplit> result = new ArrayList<>();

        for (ReceiptItem item : items) {

            List<GroupItemSplitRule> itemRules = rules.stream().filter(r -> r.getItemName().equalsIgnoreCase(item.getName())).toList();

            List<ItemSplitResult> splits;

            if (itemRules.isEmpty()) {
                splits = splitEqually(item, participants);
            } else {
                splits = splitByRules(item, participants, itemRules);
            }

            result.add(new ItemSplit(item.getId(), splits));
        }

        return result;
    }

    // -------------------------
    // Equal Split Logic
    // -------------------------
    private List<ItemSplitResult> splitEqually(ReceiptItem item, List<User> participants) {

        BigDecimal totalAmount = item.getTotalPrice();
        BigDecimal count = BigDecimal.valueOf(participants.size());

        BigDecimal share = totalAmount.divide(count, 2, RoundingMode.HALF_UP);

        List<ItemSplitResult> splits = new ArrayList<>();

        for (User user : participants) {
            splits.add(new ItemSplitResult(user, share));
        }

        return splits;
    }

    // -------------------------
    // Rule-Based Split Logic
    // -------------------------
    private List<ItemSplitResult> splitByRules(ReceiptItem item, List<User> participants, List<GroupItemSplitRule> itemRules) {

        validateRules(participants, itemRules);

        BigDecimal totalAmount = item.getTotalPrice();

        List<ItemSplitResult> splits = new ArrayList<>();

        for(GroupItemSplitRule rule : itemRules){

            BigDecimal amount = totalAmount.multiply(rule.getPercentage()).setScale(2, RoundingMode.HALF_UP);

            splits.add(new ItemSplitResult(rule.getUser(), amount));
        }

        return splits;
    }

    // -------------------------
    // Validation Logic
    // -------------------------

    private void validateRules(List<User> participants, List<GroupItemSplitRule> itemRules){

        Set<UUID> participantsIds = participants.stream().map(User::getId).collect(Collectors.toSet());

        Set<UUID> ruleUserIds = itemRules.stream().map(r -> r.getUser().getId()).collect(Collectors.toSet());

        if (!participantsIds.equals(ruleUserIds)) {
            throw new RuntimeException("Rules do not cover all particpants");
        }

        // Check Sum = 100%

        BigDecimal sum = (itemRules.stream().map(GroupItemSplitRule::getPercentage).reduce(BigDecimal.ZERO, BigDecimal::add)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        if  (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new RuntimeException("\"Rule percentages must sum to 1.0");
        }
    }
}
