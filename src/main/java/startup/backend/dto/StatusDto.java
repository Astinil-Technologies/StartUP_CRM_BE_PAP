package startup.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusDto {
    private String user_id;
    private String status;
    private String check_in_time;
    private String session_duration;
}