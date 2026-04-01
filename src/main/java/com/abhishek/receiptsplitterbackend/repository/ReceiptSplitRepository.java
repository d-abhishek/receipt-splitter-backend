package com.abhishek.receiptsplitterbackend.repository;

import com.abhishek.receiptsplitterbackend.entity.ReceiptSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceiptSplitRepository  extends JpaRepository<ReceiptSplit, UUID> {

    void deleteByReceiptId(UUID receiptId);
    List<ReceiptSplit> findByReceiptId(UUID receiptId);
}
