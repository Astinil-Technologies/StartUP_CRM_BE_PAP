package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupResponseDto {

    private Long groupId;      // Unique identifier for the group
    private String groupName;  // Name of the group
    private Long createdBy;    // ID of the user who created the group
}
