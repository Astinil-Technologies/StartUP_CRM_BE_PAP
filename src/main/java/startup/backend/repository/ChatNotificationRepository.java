package startup.backend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.entity.ChatNotification;
import startup.backend.enums.ChatNotificationStatus;

import java.util.List;

@Repository
public interface ChatNotificationRepository extends JpaRepository<ChatNotification, Long> {

    List<ChatNotification> findByRecipientIdAndStatus(Long recipientId, ChatNotificationStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE ChatNotification n SET n.status = :status WHERE n.recipientId = :userId AND n.notificationId IN :ids")
    void markAsRead(@Param("userId") Long userId,
                    @Param("ids") List<Long> ids,
                    @Param("status") ChatNotificationStatus status);
}
