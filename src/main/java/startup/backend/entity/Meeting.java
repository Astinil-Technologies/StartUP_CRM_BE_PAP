package startup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity(name = "meetings")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String hostUserId;

    @Column(name = "scheduled_for", nullable = true)
    private Instant scheduledFor;          // null means “right now”

    private Instant createdAt;

     private boolean ended;
    // or private Instant endedAt;

}

