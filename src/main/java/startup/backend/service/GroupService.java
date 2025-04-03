package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.dto.GroupDto;
import startup.backend.dto.GroupResponseDto;
import startup.backend.entity.ChatGroup;
import startup.backend.entity.User;
import startup.backend.repository.ChatGroupRepository;
import startup.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final UserRepository userRepository;

    // Create a Group
    @Transactional
    public GroupResponseDto createGroup(GroupDto groupDto) {
        User creator = userRepository.findById(groupDto.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatGroup group = new ChatGroup();
        group.setGroupName(groupDto.getGroupName());
        group.setCreatedBy(creator);
        group.setMembers(new HashSet<>(userRepository.findAllById(groupDto.getMembers())));

        ChatGroup savedGroup = chatGroupRepository.save(group);

        return new GroupResponseDto(
                savedGroup.getId(),
                savedGroup.getGroupName(),
                savedGroup.getCreatedBy().getId()
        );
    }

    // Add User to Group
    @Transactional
    public String addUserToGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        group.getMembers().add(user);
        chatGroupRepository.save(group);

        return "User added successfully to group with ID: " + groupId;
    }


    // Remove User from Group
    @Transactional
    public String removeUserFromGroup(Long groupId, Long userId) {
        // Fetch the group and user entities
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate if the user exists in the group's members
        if (!group.getMembers().contains(user)) {
            throw new RuntimeException("User is not a member of the group");
        }

        // Remove the user from the group's members set
        group.getMembers().remove(user);

        // Save the updated group
        chatGroupRepository.save(group);

        return "User removed successfully from group with ID: " + groupId;
    }


}
