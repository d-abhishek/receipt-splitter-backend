package com.abhishek.receiptsplitterbackend.repository;

import com.abhishek.receiptsplitterbackend.entity.GroupItemSplitRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupItemSplitRuleRepository extends JpaRepository<GroupItemSplitRule, UUID> {
}
