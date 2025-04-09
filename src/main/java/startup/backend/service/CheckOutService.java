package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.CheckOutDto;
import startup.backend.dto.CheckOutStatusDto;
import startup.backend.entity.CheckOut;
import startup.backend.repository.CheckOutRepository;
import startup.backend.repository.CheckInRepository;
import startup.backend.entity.CheckIn;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckOutService {
    private final CheckOutRepository checkOutRepository;
    private final CheckInRepository checkInRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CheckOutDto checkOut(String userId) {
        Optional<CheckIn> lastCheckIn = checkInRepository.findTopByUserIdOrderByCheckInTimeDesc(userId);
        if (lastCheckIn.isPresent()) {
            CheckIn checkIn = lastCheckIn.get();
            LocalDateTime checkOutTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            Duration duration = Duration.between(checkIn.getCheckInTime(), checkOutTime);
            String totalHours = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());

            CheckOut checkOut = new CheckOut(null, userId, checkIn.getCheckInTime(), checkOutTime, "OUT", totalHours);
            checkOutRepository.save(checkOut);
            return new CheckOutDto(userId, "OUT", checkOutTime.format(formatter), totalHours);
        }
        throw new RuntimeException("No check-in record found for user");
    }

    public CheckOutStatusDto getCheckOutStatus(String userId) {
        Optional<CheckOut> lastCheckOut = checkOutRepository.findTopByUserIdOrderByCheckOutTimeDesc(userId);
        if (lastCheckOut.isPresent()) {
            CheckOut checkOut = lastCheckOut.get();
            return new CheckOutStatusDto(userId, "OUT", checkOut.getCheckInTime().format(formatter), checkOut.getCheckOutTime().format(formatter), checkOut.getTotalHours());
        }
        throw new RuntimeException("No check-out record found for user");
    }
}