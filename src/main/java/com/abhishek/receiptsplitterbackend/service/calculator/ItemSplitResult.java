package com.abhishek.receiptsplitterbackend.service.calculator;

import com.abhishek.receiptsplitterbackend.entity.User;

import java.math.BigDecimal;

public class ItemSplitResult {

    private User user;
    private BigDecimal amount;

    public ItemSplitResult(User user, BigDecimal amount) {
        this.user = user;
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
