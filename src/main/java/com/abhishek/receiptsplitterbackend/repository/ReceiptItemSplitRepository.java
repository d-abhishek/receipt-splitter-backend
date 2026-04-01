package com.abhishek.receiptsplitterbackend.repository;

import com.abhishek.receiptsplitterbackend.entity.ReceiptItemSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceiptItemSplitRepository extends JpaRepository<ReceiptItemSplit, UUID> {

    void deleteByItemReceiptId(UUID receiptId);
    List<ReceiptItemSplit> findByItemReceiptId(UUID receiptId);
}
