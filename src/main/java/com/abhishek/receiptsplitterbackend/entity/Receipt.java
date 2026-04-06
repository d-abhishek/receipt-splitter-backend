package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single uploaded receipt.
 * A receipt may belong to a group (rule-based splitting)
 * or be ad-hoc (manual splitting).
 */

@Setter
@Getter
@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String description;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency = "EUR";

    @ManyToOne(optional = false)
    private User uploadedBy;

    @ManyToOne(optional = false)
    private User paidBy;

    @ManyToOne
    private Group group;

    @Column(nullable = false)
    private LocalDate receiptDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptItem> items;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
