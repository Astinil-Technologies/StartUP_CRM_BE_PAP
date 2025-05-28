package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.AttendanceDto;
import startup.backend.dto.CheckOutDto;
import startup.backend.dto.CheckOutStatusDto;
import startup.backend.entity.Attendance;
import startup.backend.entity.CheckOut;
import startup.backend.repository.AttendanceRepository;
import startup.backend.repository.CheckOutRepository;
import startup.backend.repository.CheckInRepository;
import startup.backend.entity.CheckIn;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckOutService {
    private final CheckOutRepository checkOutRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AttendanceRepository attendanceRepository;


    public CheckOutDto checkIn(String userId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime startWindow = getStartOfDayWindow(now);
        LocalDateTime endWindow = startWindow.plusDays(1).withHour(9).withMinute(25);

        if (now.isBefore(startWindow) || now.isAfter(endWindow)) {
            throw new RuntimeException("Check-in allowed only between 9:30 AM today to 9:25 AM tomorrow.");
        }

        CheckOut checkOut = new CheckOut(null, userId, now, null, "IN", null);
        checkOutRepository.save(checkOut);

        return new CheckOutDto(userId, "IN", now.format(formatter), null,null);
    }

    public CheckOutDto checkOut(String userId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime startWindow = getStartOfDayWindow(now);
        LocalDateTime endWindow = startWindow.plusDays(1).withHour(9).withMinute(25);

        if (now.isBefore(startWindow) || now.isAfter(endWindow)) {
            throw new RuntimeException("Check-out allowed only between 9:30 AM today to 9:25 AM tomorrow.");
        }

        Optional<CheckOut> lastCheckIn = checkOutRepository
                .findTopByUserIdOrderByCheckInTimeDesc(userId)
                .filter(co -> co.getCheckOutTime() == null);

        if (lastCheckIn.isPresent()) {
            CheckOut checkInRecord = lastCheckIn.get();
            Duration duration = Duration.between(checkInRecord.getCheckInTime(), now);
            String totalHours = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());

            checkInRecord.setCheckOutTime(now);
            checkInRecord.setStatus("OUT");
            checkInRecord.setTotalHours(totalHours);

            checkOutRepository.save(checkInRecord);

            calculateAndSaveAttendance(userId);

            return new CheckOutDto(userId, "OUT",checkInRecord.getCheckInTime().format(formatter), now.format(formatter), totalHours);
        } else {
            throw new RuntimeException("No active check-in found to check out from.");
        }
    }

    public CheckOutStatusDto getCheckOutStatus(String userId) {
        Optional<CheckOut> lastRecordOpt = checkOutRepository.findTopByUserIdOrderByCheckInTimeDesc(userId);

        if (lastRecordOpt.isPresent()) {
            CheckOut record = lastRecordOpt.get();
            return new CheckOutStatusDto(
                    userId,
                    record.getStatus(),
                    record.getCheckInTime().format(formatter),
                    record.getCheckOutTime() != null ? record.getCheckOutTime().format(formatter) : null,
                    record.getTotalHours()
            );
        }

        throw new RuntimeException("No check-in or check-out record found for user");
    }

    private LocalDateTime getStartOfDayWindow(LocalDateTime now) {
        LocalDateTime today930 = now.withHour(9).withMinute(30).withSecond(0).withNano(0);
        return now.isBefore(today930) ? today930.minusDays(1) : today930;
    }

    public AttendanceDto calculateAndSaveAttendance(String userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime start = today.atTime(9, 30);
        LocalDateTime end = start.plusDays(1).withHour(9).withMinute(25);

        // Get all check-out records for this user in today's window
        List<CheckOut> checkOuts = checkOutRepository.findByUserIdAndCheckInTimeBetween(userId, start, end);

        Duration totalDuration = Duration.ZERO;
        for (CheckOut c : checkOuts) {
            if (c.getCheckOutTime() != null) {
                Duration sessionDuration = Duration.between(c.getCheckInTime(), c.getCheckOutTime());
                totalDuration = totalDuration.plus(sessionDuration);
            }
        }

        // Convert totalDuration to HH:mm:ss
        long totalSeconds = totalDuration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String totalTimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        String dateStr = today.toString();
        String status = totalDuration.compareTo(Duration.ofHours(8)) >= 0 ? "Present" : "Absent";

        Optional<Attendance> existingOpt = attendanceRepository.findByUserIdAndDate(userId, dateStr);
        Attendance attendance;

        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            attendance.setAttendance_status(status);
            attendance.setTotalHoursTime(totalTimeFormatted);
        } else {
            attendance = new Attendance();
            attendance.setUserId(userId);
            attendance.setDate(dateStr);
            attendance.setAttendance_status(status);
            attendance.setTotalHoursTime(totalTimeFormatted);
        }

        attendanceRepository.save(attendance);

        return new AttendanceDto(attendance.getId(), userId, status, dateStr, totalTimeFormatted);
    }



    public List<Map<String, Object>> getWeeklyAttendanceData(String userId) {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDate sevenDaysAgo = today.minusDays(6);

        List<Map<String, Object>> weeklyData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            LocalDateTime start = date.atTime(9, 30);
            LocalDateTime end = start.plusDays(1).withHour(9).withMinute(25);

            String dayOfWeek = date.getDayOfWeek().toString();
            Map<String, Object> dayRecord = new HashMap<>();
            dayRecord.put("userId", userId);
            dayRecord.put("date", date.toString());

            if (dayOfWeek.equals("SATURDAY") || dayOfWeek.equals("SUNDAY")) {
                dayRecord.put("attendance_status", "Weekend");
                dayRecord.put("first_check_in_time", null);
                dayRecord.put("last_check_out_time", null);
                dayRecord.put("total_hours_time", "0:00");
            } else {
                Optional<Attendance> attendanceOpt = attendanceRepository.findByUserIdAndDate(userId, date.toString());
                List<CheckOut> checkOuts = checkOutRepository.findByUserIdAndCheckInTimeBetween(userId, start, end);

                LocalDateTime firstCheckIn = checkOuts.stream()
                        .map(CheckOut::getCheckInTime)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);

                LocalDateTime lastCheckOut = checkOuts.stream()
                        .map(CheckOut::getCheckOutTime)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                String formattedCheckIn = firstCheckIn != null ? firstCheckIn.format(formatter) : null;
                String formattedCheckOut = lastCheckOut != null ? lastCheckOut.format(formatter) : null;

                String totalHoursTime = attendanceOpt.map(Attendance::getTotalHoursTime).orElse("0:00");

                dayRecord.put("attendance_status", attendanceOpt.map(Attendance::getAttendance_status).orElse("Absent"));
                dayRecord.put("first_check_in_time", formattedCheckIn);
                dayRecord.put("last_check_out_time", formattedCheckOut);
                dayRecord.put("total_hours_time", totalHoursTime != null ? totalHoursTime : "0:00");
            }

            weeklyData.add(dayRecord);
        }

        return weeklyData;
    }
}