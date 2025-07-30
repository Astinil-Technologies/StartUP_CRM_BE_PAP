package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startup.backend.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
//    List<ChatMessage> findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(String sender1, String receiver1, String sender2, String receiver2);

    @Query("SELECT c FROM ChatMessage c WHERE (c.senderId = :sender1 AND c.receiverId = :receiver1) OR (c.senderId = :sender2 AND c.receiverId = :receiver2)")
    List<ChatMessage> findChatBetweenUsers(@Param("sender1") String sender1,
                                           @Param("receiver1") String receiver1,
                                           @Param("sender2") String sender2,
                                           @Param("receiver2") String receiver2);

}