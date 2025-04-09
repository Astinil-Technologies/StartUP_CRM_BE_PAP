package startup.backend.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.CheckInDto;
import startup.backend.dto.StatusDto;
import startup.backend.entity.CheckIn;
import startup.backend.repository.CheckInRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInService {
    private final CheckInRepository checkInRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CheckInDto checkIn(String userId) {
        LocalDateTime checkInTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        CheckIn checkIn = new CheckIn(null, userId, checkInTime, "IN");
        checkInRepository.save(checkIn);
        return new CheckInDto(userId, "IN", checkInTime.format(formatter));
    }

    public StatusDto getStatus(String userId) {
        Optional<CheckIn> lastCheckIn = checkInRepository.findTopByUserIdOrderByCheckInTimeDesc(userId);
        if (lastCheckIn.isPresent()) {
            CheckIn checkIn = lastCheckIn.get();
            Duration duration = Duration.between(checkIn.getCheckInTime(), LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            String formattedDuration = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
            return new StatusDto(userId, checkIn.getStatus(), checkIn.getCheckInTime().format(formatter), formattedDuration);
        }
        throw new RuntimeException("No check-in record found for user");
    }
}