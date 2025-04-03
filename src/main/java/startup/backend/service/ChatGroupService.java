//package startup.backend.service;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import startup.backend.entity.ChatGroup;
//import startup.backend.entity.User;
//import startup.backend.repository.ChatGroupRepository;
//import startup.backend.repository.UserRepository;
//
//@Transactional
//@Service
//@RequiredArgsConstructor
//public class ChatGroupService {
//
//    private final ChatGroupRepository chatGroupRepository;
//    private final UserRepository userRepository;
//
//    /**
//     * Add a user to the chat group.
//     *
//     * @param groupId The ID of the chat group.
//     * @param userId  The ID of the user to be added.
//     * @return A message indicating the result.
//     */
//    @Transactional
//    public String addUserToGroup(Long groupId, Long userId) {
//        // Fetch the chat group
//        ChatGroup group = chatGroupRepository.findById(groupId)
//                .orElseThrow(() -> new RuntimeException("Chat group not found"));
//
//        // Fetch the user
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Add the user to the group
//        group.getMembers().add(user);
//
//        // Save the updated group
//        chatGroupRepository.save(group);
//
//        return "User with ID " + userId + " successfully added to group with ID " + groupId;
//    }
//}
