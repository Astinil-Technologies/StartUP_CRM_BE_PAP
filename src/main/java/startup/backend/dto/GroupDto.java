package startup.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // No-args constructor for JSON deserialization
public class GroupDto {

    private String groupName; // Name of the group
    private Long createdBy;   // ID of the user who created the group
    private List<Long> members; // A list of user IDs representing group members

    // Parameterized constructor
    public GroupDto(String groupName, Long createdBy, List<Long> members) {
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.members = members;
    }

    // Validation logic
    public void validate() {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new RuntimeException("Group name cannot be empty");
        }
        if (createdBy == null) {
            throw new RuntimeException("CreatedBy ID cannot be null");
        }
        if (members == null || members.isEmpty()) {
            throw new RuntimeException("Members list cannot be empty");
        }
    }
}
