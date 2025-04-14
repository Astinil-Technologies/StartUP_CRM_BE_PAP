package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startup.backend.entity.ChatGroup;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {

    // ✅ Corrected method name and query
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
            "FROM ChatGroup g JOIN g.members m " +
            "WHERE g.id = :groupId AND m.id = :userId")
    boolean existsByIdAndMembers_Id(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
