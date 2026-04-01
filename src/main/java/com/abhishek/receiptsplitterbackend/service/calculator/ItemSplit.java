package com.abhishek.receiptsplitterbackend.service.calculator;

import java.util.List;
import java.util.UUID;

public class ItemSplit {

    private UUID itemId;
    private List<ItemSplitResult> splits;

    ItemSplit(UUID itemId, List<ItemSplitResult> splits) {
        this.itemId = itemId;
        this.splits = splits;
    }

    public UUID getItemId() {
        return itemId;
    }
    public List<ItemSplitResult> getSplits() {
        return splits;
    }
}
