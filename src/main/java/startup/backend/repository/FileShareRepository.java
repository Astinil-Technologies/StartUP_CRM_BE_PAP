package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startup.backend.entity.FileShare;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {


    void deleteByFileId(Long fileId);


}
