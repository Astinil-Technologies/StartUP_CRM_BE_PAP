package startup.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor

public class FileDto {
    private Long id;
    private String name;
    private String type;
    private Long size;
    private String sharedByName;
    private LocalDateTime uploadedAt;

    public FileDto(Long id, String name, String type, Long size, String sharedByName, LocalDateTime uploadedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.sharedByName = sharedByName;
        this.uploadedAt = uploadedAt;
    }
}
