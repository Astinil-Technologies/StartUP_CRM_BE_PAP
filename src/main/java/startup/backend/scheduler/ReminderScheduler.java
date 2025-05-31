package startup.backend.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import startup.backend.entity.Reminder;
import startup.backend.repository.ReminderRepository;
import startup.backend.service.ReminderServiceImpl;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;
    private final ReminderServiceImpl reminderService;

    @Scheduled(fixedRate = 60000)
    public void checkAndTriggerReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findAll().stream()
                .filter(r -> r.getDueDateTime().isBefore(now.plusSeconds(30)) &&
                        r.getDueDateTime().isAfter(now.minusSeconds(30)))
                .toList();
        for (Reminder reminder : dueReminders) {
            System.out.println("🔔 Reminder Triggered: " + reminder.getTitle() + " | Due at: " + reminder.getDueDateTime());
            reminder.setNotified(true);
            reminderRepository.save(reminder);
            reminderService.handleRecurringReminder(reminder);
        }
    }
    @Scheduled(fixedRate = 60000)
    public void deleteExpiredReminders() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        reminderRepository.deleteExpiredReminders(oneHourAgo);
        System.out.println("🗑️ Deleted expired reminders older than: " + oneHourAgo);
    }
}