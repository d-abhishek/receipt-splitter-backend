package com.abhishek.receiptsplitterbackend.Dto;

import java.util.List;
import java.util.UUID;

public class CreateGroupRequest {

    private String groupName;
    private List<UUID> memberIds;


    public CreateGroupRequest(String groupName, List<UUID> memberIds) {
        this.groupName = groupName;
        this.memberIds = memberIds;
    }

    // Getters and Setters

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<UUID> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<UUID> memberIds) {
        this.memberIds = memberIds;
    }
}
