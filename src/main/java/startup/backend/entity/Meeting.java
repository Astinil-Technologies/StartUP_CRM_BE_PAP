package startup.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String meetingId;

    private String title;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne
    @JsonIgnore
    private User host;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> participants;

    private boolean locked;
    private boolean started;
    private boolean lobbyEnabled;

    @PrePersist
    public void prePersist() {
        this.meetingId = java.util.UUID.randomUUID().toString();
        this.started = false;
        this.locked = false;
        this.lobbyEnabled = true;
    }
}
