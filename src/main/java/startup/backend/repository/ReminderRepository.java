package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.entity.Reminder;
import startup.backend.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUser(User user);
    Optional<Reminder> findByAttachmentPath(String attachmentPath);
    @Modifying
    @Transactional
    @Query("DELETE FROM Reminder r WHERE r.dueDateTime < :cutoff")
    void deleteExpiredReminders(LocalDateTime cutoff);
}