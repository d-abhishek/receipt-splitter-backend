package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a total-level split of a receipt among users.
 * Primarily used for ad-hoc receipts without item-level splitting.
 */

@Setter
@Getter
@Entity
@Table(name = "receipt_splits")
public class ReceiptSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Receipt receipt;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amountOwed;

    private boolean paid = false;
}
