package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Represents an individual item on a receipt.
 * Items can be split independently among users
 * using either group rules or manual adjustments.
 */

@Setter
@Getter
@Entity
@Table(name = "receipt_items")
public class ReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Receipt receipt;

    @Column(nullable = false)
    private String name;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal finalPrice;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptItemSplit> splits;
}
