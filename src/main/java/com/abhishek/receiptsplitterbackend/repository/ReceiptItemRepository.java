package com.abhishek.receiptsplitterbackend.repository;

import com.abhishek.receiptsplitterbackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReceiptItemRepository extends JpaRepository<Payment, UUID> {
}
