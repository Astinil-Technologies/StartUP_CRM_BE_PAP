package startup.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_out")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;
    private String totalHours;
}
