package startup.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startup.backend.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.recipient.id = :recipientUserId OR m.group.id = :groupId ORDER BY m.timestamp DESC")
    List<Message> findMessagesByRecipientOrGroup(@Param("recipientUserId") Long recipientUserId,
                                                 @Param("groupId") Long groupId,
                                                 Pageable pageable);
}
