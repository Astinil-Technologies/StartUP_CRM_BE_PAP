package startup.backend.entity;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "check_in")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private LocalDateTime checkInTime;
    private String status;
}