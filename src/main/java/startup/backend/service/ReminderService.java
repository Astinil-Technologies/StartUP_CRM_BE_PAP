package startup.backend.service;

import startup.backend.dto.ReminderDto;
import startup.backend.entity.Reminder;
import startup.backend.entity.User;

import java.util.List;

public interface ReminderService {
    Reminder createReminder(ReminderDto dto, User user);
    List<Reminder> getReminders(User user);
    Reminder updateReminder(Long id, ReminderDto dto, User user);
    void deleteReminder(Long id, User user);
}
