package com.abhishek.receiptsplitterbackend.controller;

import com.abhishek.receiptsplitterbackend.Dto.GroupRequest;
import com.abhishek.receiptsplitterbackend.entity.Group;
import com.abhishek.receiptsplitterbackend.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Create a new group with members.
     *
     * @param groupRequest Request body with groupName and memberIds
     * @return ResponseEntity containing the created Group
     */

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody GroupRequest groupRequest) {
        try {
            Group createdGroup = groupService.createGroup(groupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("Group created successfully with ID: " + createdGroup.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all groups.
     *
     * @return ResponseEntity containing list of all groups
     */

    @GetMapping("/list")
    public ResponseEntity<List<String>> listGroup(){
        try{
            List<String> groups = groupService.listGroups();

            if (groups.isEmpty()){
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific group by ID.
     *
     * @param groupId The UUID of the group
     * @return ResponseEntity containing the group
     */

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable UUID groupId){
        try{
            Group group = groupService.getGroupById(groupId);

            if (group == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(group);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add members to an existing group.
     *
     * @param groupId The UUID of the group
     * @param memberIds List of user IDs to add
     * @return ResponseEntity containing the updated group
     */

    @PostMapping("/{groupId}/add-members")
    public ResponseEntity<Group> addMembersToGroup(@PathVariable UUID groupId, @RequestBody List<UUID> memberIds){
        try{
            Group updatedGroup = groupService.addMembersToGroup(groupId, memberIds);
            return ResponseEntity.status(HttpStatus.OK).body(updatedGroup);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete members from an existing group.
     *
     * @param groupId The UUID of the group
     * @param memberIds List of user IDs to add
     * @return ResponseEntity containing the updated group
     */

    @PostMapping("/{groupId}/delete-members")
    public ResponseEntity<Group> deleteMembersFromGroup(@PathVariable UUID groupId, @RequestBody List<UUID> memberIds){
        try{
            Group updatedGroup = groupService.deleteMembersFromGroup(groupId, memberIds);
            return ResponseEntity.status(HttpStatus.OK).body(updatedGroup);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable UUID groupId){

        Group group = groupService.getGroupById(groupId);

        if (group == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            groupService.deleteGroup(groupId);
        }

        return ResponseEntity.ok("Group deleted successfully");
    }
}
