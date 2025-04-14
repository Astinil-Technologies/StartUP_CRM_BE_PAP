package startup.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MessageDto {

    @NotNull(message = "Sender ID cannot be null")
    private Long senderId; // ID of the sender

    private Long recipientId; // Optional for group messages

    private Long groupId; // Optional for one-on-one messages

    @NotBlank(message = "Message content cannot be blank")
    private String content; // Content of the message


    private MultipartFile file;

}
