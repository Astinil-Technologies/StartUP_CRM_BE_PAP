package startup.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import startup.backend.enums.AccessType;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private FileRecord file;

    @ManyToOne
    private User sharedWith;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    private LocalDateTime expiresAt;
}
