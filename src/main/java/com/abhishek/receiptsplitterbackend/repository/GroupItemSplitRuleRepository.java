package com.abhishek.receiptsplitterbackend.repository;

import com.abhishek.receiptsplitterbackend.entity.GroupItemSplitRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupItemSplitRuleRepository extends JpaRepository<GroupItemSplitRule, UUID> {

    List<GroupItemSplitRule> findByGroupId(UUID groupId);
    Optional<GroupItemSplitRule> findByGroupIdAndItemName(UUID groupId, String itemName);
}
