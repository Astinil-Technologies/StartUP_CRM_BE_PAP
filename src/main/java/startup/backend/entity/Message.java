package startup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor // Needed for JPA
@AllArgsConstructor
@Builder // Allows builder pattern
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ChatGroup group;


    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;


    @Builder
    public Message(Long id, User sender, User recipient, ChatGroup group, String content, Instant timestamp, boolean isDeleted) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.group = group;
        this.content = content;
        this.timestamp = timestamp;
        this.isDeleted = isDeleted;
    }



    //for file upload
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;



}
