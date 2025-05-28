package startup.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckOutDto {
    private String user_id;
    private String status;
    private String check_in_time;
    private String check_out_time;
    private String total_hours;
}

