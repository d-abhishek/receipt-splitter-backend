package com.abhishek.receiptsplitterbackend.Dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GroupRequest {

    private String groupName;
    private List<UUID> memberIds;
}
