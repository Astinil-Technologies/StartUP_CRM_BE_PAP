package startup.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import startup.backend.dto.AddUserRequestDto;
import startup.backend.dto.GroupDto;
import startup.backend.dto.GroupResponseDto;
import startup.backend.service.GroupService;

@RestController
@RequestMapping("/api/chat/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // Create a New Group
    @PostMapping("/create")
    public ResponseEntity<GroupResponseDto> createGroup(@RequestBody GroupDto groupDto) {
        groupDto.validate(); // Validate input data
        GroupResponseDto createdGroup = groupService.createGroup(groupDto);
        return ResponseEntity.ok(createdGroup);
    }

    @PostMapping("/{groupId}/add")
    public ResponseEntity<?> addUserToGroup(@PathVariable Long groupId, @RequestBody AddUserRequestDto addUserRequestDto) {
        Long userId = addUserRequestDto.getUserId(); // Extract userId from DTO
        String message = groupService.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(new AddUserResponse(groupId, message));
    }


    // Remove a User from a Group
    @DeleteMapping("/{groupId}/remove/{userId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        String message = groupService.removeUserFromGroup(groupId, userId);
        return ResponseEntity.ok(new RemoveUserResponse(message));
    }

    // DTO for Add User Response
    private static class AddUserResponse {
        private final Long groupId;
        private final String message;

        public AddUserResponse(Long groupId, String message) {
            this.groupId = groupId;
            this.message = message;
        }

        public Long getGroupId() {
            return groupId;
        }

        public String getMessage() {
            return message;
        }
    }

    // DTO for Remove User Response
    private static class RemoveUserResponse {
        private final String message;

        public RemoveUserResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
