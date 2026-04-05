package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents the final applied split for a specific receipt item
 * and user. This stores the calculated amount owed after
 * applying rules or manual modifications.
 */

@Setter
@Getter
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
}
