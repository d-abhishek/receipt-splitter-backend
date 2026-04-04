package com.abhishek.receiptsplitterbackend.service;

import com.abhishek.receiptsplitterbackend.Dto.CreateGroupRequest;
import com.abhishek.receiptsplitterbackend.entity.Group;
import com.abhishek.receiptsplitterbackend.entity.User;
import com.abhishek.receiptsplitterbackend.repository.GroupRepository;
import com.abhishek.receiptsplitterbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new group with the specified name and members.
     *
     * @param createGroupRequest CreateGroupRequest containing groupName and memberIds
     * @return The created Group entity with members
     * @throws IllegalArgumentException if group name is empty or invalid
     */

    public Group createGroup(CreateGroupRequest createGroupRequest) {

        // Validate input
        if (createGroupRequest.getGroupName() == null || createGroupRequest.getGroupName().trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        List<UUID> memberIds = createGroupRequest.getMemberIds();
        List<User> members = userRepository.findAllById(memberIds);

        if (members.isEmpty() && !memberIds.isEmpty()) {
            throw new IllegalArgumentException("No valid users found for the provided member IDs");
        }

        Group group = new Group();
        group.setName(createGroupRequest.getGroupName().trim());
        group.setMembers(new HashSet<>(members));

        return groupRepository.save(group);
    }

    /**
     * Retrieves all groups.
     *
     * @return List of all groups
     */

    public List<String> listGroups() {
        return groupRepository.findAll().stream().map(Group::getName).toList();
    }

    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
    }

    /**
     * Adds members to an existing group.
     *
     * @param groupId The UUID of the group
     * @param memberIds List of user IDs to add
     * @return Updated Group entity
     */

    public Group addMembersToGroup(UUID groupId, List<UUID> memberIds) {
        Group group = getGroupById(groupId);

        List<User> members = userRepository.findAllById(memberIds);

        if (members.isEmpty()) {
            throw new IllegalArgumentException("No valid users found for the provided member IDs");
        }

        group.getMembers().addAll(members);

        return groupRepository.save(group);
    }

    /**
     * Delete members from an existing group.
     *
     * @param groupId The UUID of the group
     * @param memberIds List of user IDs to delete
     * @return Updated Group entity
     */

    public Group deleteMembersFromGroup(UUID groupId, List<UUID> memberIds) {
        Group group = getGroupById(groupId);

        List<User> members = userRepository.findAllById(memberIds);

        if (members.isEmpty()) {
            throw new IllegalArgumentException("No valid users found for the provided member IDs");
        }

        members.forEach(group.getMembers()::remove);

        return groupRepository.save(group);
    }


    /**
     * Deletes group by ID.
     *
     * @param groupID The UUID of the group
     */

    public void deleteGroup(UUID groupID){

        try {
            Group group = getGroupById(groupID);
            groupRepository.delete(group);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Group not found with ID: " + groupID);
        }
    }
}
