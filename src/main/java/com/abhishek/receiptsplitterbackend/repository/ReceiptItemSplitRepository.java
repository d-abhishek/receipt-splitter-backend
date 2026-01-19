package com.abhishek.receiptsplitterbackend.repository;

import com.abhishek.receiptsplitterbackend.entity.ReceiptItemSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReceiptItemSplitRepository extends JpaRepository<ReceiptItemSplit, UUID> {
}
