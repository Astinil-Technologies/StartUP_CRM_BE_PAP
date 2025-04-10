package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import startup.backend.dto.MessageDto;
import startup.backend.dto.MessageResponseDto;
import startup.backend.entity.ChatGroup;
import startup.backend.entity.Message;
import startup.backend.entity.User;
import startup.backend.repository.ChatGroupRepository;
import startup.backend.repository.MessageRepository;
import startup.backend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
            .withZone(ZoneId.of("Asia/Kolkata")); // Indian Standard Time

    public MessageResponseDto sendMessage(MessageDto messageDto) {
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = (messageDto.getRecipientId() != null) ?
                userRepository.findById(messageDto.getRecipientId()).orElse(null) : null;

        ChatGroup group = (messageDto.getGroupId() != null) ?
                chatGroupRepository.findById(messageDto.getGroupId()).orElse(null) : null;

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .group(group)
                .content(messageDto.getContent())
                .timestamp(Instant.now())
                .isDeleted(false) // ✅ Add this
                .build();


        Message savedMessage = messageRepository.save(message);

        return new MessageResponseDto(
                savedMessage.getId(),
                savedMessage.getSender().getId(),
                (savedMessage.getRecipient() != null) ? savedMessage.getRecipient().getId() : null,
                savedMessage.getContent(),
                FORMATTER.format(savedMessage.getTimestamp())
        );
    }


    // ✅ Get chat history
    public List<MessageResponseDto> getMessages(Long conversationId, Pageable pageable) {
        List<Message> messages = messageRepository.findMessagesByRecipientOrGroup(conversationId, conversationId, pageable);

        return messages.stream()
                .map(message -> new MessageResponseDto(
                        message.getId(),
                        message.getSender().getId(),
                        (message.getRecipient() != null) ? message.getRecipient().getId() : null,
                        message.getContent(),
                        FORMATTER.format(message.getTimestamp())
                ))
                .collect(Collectors.toList());
    }

    // ✅ Soft delete
    public String softDeleteMessage(Long messageId, Long requesterId) throws AccessDeniedException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(requesterId)) {
            throw new AccessDeniedException("Not authorized to delete this message");
        }

        message.setDeleted(true);
        messageRepository.save(message);

        return "Message soft-deleted successfully.";
    }

    // ✅ Hard delete
    public String hardDeleteMessage(Long messageId, Long requesterId, boolean isAdmin) throws AccessDeniedException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!isAdmin && !message.getSender().getId().equals(requesterId)) {
            throw new AccessDeniedException("Only admins can hard delete others' messages");
        }

        messageRepository.delete(message);
        return "Message hard-deleted successfully.";
    }

    public boolean isUserInConversation(Long userId, Long conversationId) {
        // One-to-one chat check (both directions)
        boolean isDirectChat = messageRepository.existsBySender_IdAndRecipient_Id(userId, conversationId)
                || messageRepository.existsBySender_IdAndRecipient_Id(conversationId, userId);

        // ✅ Use corrected method
        boolean isGroupChat = chatGroupRepository.existsByIdAndMembers_Id(conversationId, userId);

        return isDirectChat || isGroupChat;
    }




    public MessageResponseDto sendMessageWithFile(Long senderId, Long recipientId, Long groupId, String content, MultipartFile file) throws IOException {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = (recipientId != null) ? userRepository.findById(recipientId).orElse(null) : null;
        ChatGroup group = (groupId != null) ? chatGroupRepository.findById(groupId).orElse(null) : null;

        String fileName = null;
        String fileType = null;
        String fileUrl = null;
        Long fileSize = null;

        if (file != null && !file.isEmpty()) {
            if (!List.of("image/jpeg", "image/png", "application/pdf").contains(file.getContentType())) {
                throw new RuntimeException("Invalid file type");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("File size exceeds 10MB limit");
            }

            fileName = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            fileType = file.getContentType();
            fileSize = file.getSize();

            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            fileUrl = "/uploads/" + fileName;
        }

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .group(group)
                .content(content)
                .timestamp(Instant.now())
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .fileUrl(fileUrl)
                .isDeleted(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        return new MessageResponseDto(
                savedMessage.getId(),
                savedMessage.getSender().getId(),
                recipient != null ? recipient.getId() : null,
                savedMessage.getContent(),
                FORMATTER.format(savedMessage.getTimestamp()),
                savedMessage.getFileName(),
                savedMessage.getFileType(),
                savedMessage.getFileUrl(),
                savedMessage.getFileSize()
        );
    }

}
