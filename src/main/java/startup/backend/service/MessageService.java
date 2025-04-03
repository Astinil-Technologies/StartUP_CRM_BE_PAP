package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import startup.backend.dto.MessageDto;
import startup.backend.dto.MessageResponseDto;
import startup.backend.entity.ChatGroup;
import startup.backend.entity.Message;
import startup.backend.entity.User;
import startup.backend.repository.ChatGroupRepository;
import startup.backend.repository.MessageRepository;
import startup.backend.repository.UserRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
            .withZone(ZoneId.of("Asia/Kolkata")); // Indian Standard Time

    public MessageResponseDto sendMessage(MessageDto messageDto) {
        // Fetch the sender, throw exception if not found
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Fetch the recipient if provided
        User recipient = (messageDto.getRecipientId() != null) ?
                userRepository.findById(messageDto.getRecipientId()).orElse(null) : null;

        // Fetch the group if provided
        ChatGroup group = (messageDto.getGroupId() != null) ?
                chatGroupRepository.findById(messageDto.getGroupId()).orElse(null) : null;

        // Create and populate the message entity
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .group(group)
                .content(messageDto.getContent())
                .timestamp(Instant.now())
                .build();

        // Save the message and construct the response
        Message savedMessage = messageRepository.save(message);

        return new MessageResponseDto(
                savedMessage.getId(),
                savedMessage.getSender().getId(),
                (savedMessage.getRecipient() != null) ? savedMessage.getRecipient().getId() : null,
                savedMessage.getContent(),
                FORMATTER.format(savedMessage.getTimestamp())
        );
    }

    public List<MessageResponseDto> getMessages(Long conversationId, Pageable pageable) {
        // Fetch messages for the given conversationId
        List<Message> messages = messageRepository.findMessagesByRecipientOrGroup(conversationId, conversationId, pageable);

        // Map messages to their DTO format
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
}
