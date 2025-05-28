package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private long id;
    private String userId;
    private String attendance_status;
    private String date;
    private String  total_hours_time;
}
