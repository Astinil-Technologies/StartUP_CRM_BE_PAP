package startup.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import startup.backend.entity.ChatNotification;
import startup.backend.enums.ChatNotificationStatus;
import startup.backend.repository.ChatNotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatNotificationService {

    @Autowired
    private ChatNotificationRepository notificationRepo;

    public void generateNotifications(Long messageId, Long senderId, List<Long> recipientIds)
    {
        LocalDateTime now = LocalDateTime.now();
        List<ChatNotification> notifications = recipientIds.stream().map(recipientId ->
                new ChatNotification(messageId, senderId, recipientId, now, ChatNotificationStatus.UNREAD)
        ).toList();

        notificationRepo.saveAll(notifications);
    }

    public List<ChatNotification> getUnreadNotifications(Long userId) {
        List<ChatNotification> result = notificationRepo.findByRecipientIdAndStatus(userId, ChatNotificationStatus.UNREAD);
        System.out.println("Fetched " + result.size() + " unread notifications for userId: " + userId);
        return result;
    }

    @Transactional
    public void markNotificationsAsRead(Long userId, List<Long> notificationIds) {
        notificationRepo.markAsRead(userId, notificationIds, ChatNotificationStatus.READ);
    }
}