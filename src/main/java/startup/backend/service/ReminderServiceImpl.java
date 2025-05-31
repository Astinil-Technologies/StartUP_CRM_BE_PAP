package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.dto.ReminderDto;
import startup.backend.entity.Reminder;
import startup.backend.entity.User;
import startup.backend.repository.ReminderRepository;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;

    @Override
    public Reminder createReminder(ReminderDto dto, User user) {
        Reminder reminder = Reminder.builder()
                .title(dto.getTitle())
                .dueDateTime(dto.getDueDateTime())
                .notifyBeforeMinutes(dto.getNotifyBeforeMinutes())
                .attachmentPath(dto.getAttachmentPath())
                .user(user)
                .recurring(dto.getRecurring())
                .recurringType(dto.getRecurringType())
                .build();
        return reminderRepository.save(reminder);
    }

    @Override
    public List<Reminder> getReminders(User user) {
        return reminderRepository.findByUser(user);
    }

    @Override
    public Reminder updateReminder(Long id, ReminderDto dto, User user) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        if (!reminder.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        reminder.setTitle(dto.getTitle());
        reminder.setDueDateTime(dto.getDueDateTime());
        reminder.setNotifyBeforeMinutes(dto.getNotifyBeforeMinutes());
        if (reminder.getAttachmentPath() != null && !reminder.getAttachmentPath().equals(dto.getAttachmentPath())) {
            File oldFile = new File(reminder.getAttachmentPath());
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
        reminder.setAttachmentPath(dto.getAttachmentPath());
        reminder.setRecurring(dto.getRecurring());
        reminder.setRecurringType(dto.getRecurringType());
        return reminderRepository.save(reminder);
    }

    @Override
    public void deleteReminder(Long id, User user) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        if (!reminder.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (reminder.getAttachmentPath() != null) {
            File file = new File(reminder.getAttachmentPath());
            if (file.exists()) {
                file.delete();
            }
        }
        reminderRepository.delete(reminder);
    }
    public void handleRecurringReminder(Reminder reminder) {
        if (!Boolean.TRUE.equals(reminder.getRecurring())) return;
        LocalDateTime nextDueDate = switch (reminder.getRecurringType()) {
            case DAILY -> reminder.getDueDateTime().plusDays(1);
            case WEEKLY -> reminder.getDueDateTime().plusWeeks(1);
            case MONTHLY -> reminder.getDueDateTime().plusMonths(1);
        };
        Reminder nextReminder = Reminder.builder()
                .title(reminder.getTitle())
                .dueDateTime(nextDueDate)
                .notifyBeforeMinutes(reminder.getNotifyBeforeMinutes())
                .attachmentPath(reminder.getAttachmentPath())
                .user(reminder.getUser())
                .recurring(true)
                .recurringType(reminder.getRecurringType())
                .build();
        reminderRepository.save(nextReminder);
    }
}

