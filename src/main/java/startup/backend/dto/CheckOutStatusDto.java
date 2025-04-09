package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckOutStatusDto {
    private String user_id;
    private String status;
    private String last_check_in;
    private String last_check_out;
    private String total_hours_today;
}