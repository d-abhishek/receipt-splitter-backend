package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a payment made by a user toward a receipt.
 * Used to track settlements and outstanding balances.
 */

@Setter
@Getter
@Entity
@Table(name = "payments")
public class Payment {

    // Getters & Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Receipt receipt;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    private LocalDateTime paidAt;

    @PrePersist
    void onPay() {
        paidAt = LocalDateTime.now();
    }
}
