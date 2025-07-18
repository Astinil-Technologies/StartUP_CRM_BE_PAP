package startup.backend.scheduler;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import startup.backend.entity.Meeting;
import startup.backend.repository.MeetingRepository;
import startup.backend.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MeetingReminderJob extends QuartzJobBean {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EmailService emailService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesLater = now.plusMinutes(10);

        List<Meeting> upcomingMeetings = meetingRepository.findByStartTimeBetween(now, tenMinutesLater);

        for (Meeting meeting : upcomingMeetings) {
            String subject = "Reminder: Upcoming Meeting";
            String body = "Your meeting '" + meeting.getTitle() + "' starts at " + meeting.getStartTime();
            String recipient = meeting.getHost().getEmail();
            emailService.sendSimpleMessage(recipient, subject, body);
        }
    }
}
