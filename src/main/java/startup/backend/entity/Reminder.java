package startup.backend.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import startup.backend.enums.RecurringType;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "due_date_time", nullable = false)
    private LocalDateTime dueDateTime;

    @Column(name = "notify_before_minutes", nullable = false)
    private Integer notifyBeforeMinutes;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @JsonProperty("recurring")
    @Column(name = "is_recurring")
    private Boolean recurring;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_type")
    private RecurringType recurringType;

    @Column(name = "notified")
    private Boolean notified = false;
}