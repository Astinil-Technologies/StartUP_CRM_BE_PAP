package startup.backend.dto;

import lombok.Data;

@Data
public class AddUserRequestDto {
    private Long userId; // This maps to the `userId` field in your JSON object
}
