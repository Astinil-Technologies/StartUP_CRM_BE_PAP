package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.ChatGroup;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
}
