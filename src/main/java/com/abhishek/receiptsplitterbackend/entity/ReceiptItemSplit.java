package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents the final applied split for a specific receipt item
 * and user. This stores the calculated amount owed after
 * applying rules or manual modifications.
 */

@Entity
@Table(name = "receipt_item_splits")
public class ReceiptItemSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private ReceiptItem item;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amountOwed;

    private Boolean paid = false;

    // Getters & Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ReceiptItem getItem() {
        return item;
    }

    public void setItem(ReceiptItem item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(BigDecimal amountOwed) {
        this.amountOwed = amountOwed;
    }

    public Boolean isPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}
