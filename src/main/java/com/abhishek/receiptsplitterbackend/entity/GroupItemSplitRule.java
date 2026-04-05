package com.abhishek.receiptsplitterbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Defines a persistent item-level split rule for a specific group.
 * When a receipt is uploaded for the group, matching items
 * automatically apply these split percentages.
 * <p>
 * Example: In a group, "Milk" is always split 70/30 between two users.
 */

@Setter
@Getter
@Entity
@Table(name = "group_item_split_rule")
public class GroupItemSplitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false)
    private String itemName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal percentage;
}
