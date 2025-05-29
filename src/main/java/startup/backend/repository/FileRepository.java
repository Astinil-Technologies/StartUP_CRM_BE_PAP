package startup.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startup.backend.dto.FileDto;
import startup.backend.entity.FileRecord;

public interface FileRepository extends JpaRepository<FileRecord, Long> {

    @Query("SELECT new startup.backend.dto.FileDto(f.id, f.name, f.type, f.size, CONCAT(f.sharedBy.firstName, ' ', f.sharedBy.lastName), f.uploadedAt) " +
            "FROM FileRecord f WHERE f.receiver.id = :receiverId")
    Page<FileDto> fetchReceivedLight(@Param("receiverId") Long receiverId, Pageable pageable);



}
