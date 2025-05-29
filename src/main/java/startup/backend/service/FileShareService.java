package startup.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.entity.FileRecord;
import startup.backend.entity.FileShare;
import startup.backend.entity.User;
import startup.backend.enums.AccessType;
import startup.backend.repository.FileRepository;
import startup.backend.repository.FileShareRepository;
import startup.backend.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileShareService {

    private final FileShareRepository fileShareRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public void shareFile(Long fileId, Long sharedWithUserId, AccessType accessType, LocalDateTime expiresAt) {
        FileRecord file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User sharedWith = userRepository.findById(sharedWithUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileShare share = FileShare.builder()
                .file(file)
                .sharedWith(sharedWith)
                .accessType(accessType)
                .expiresAt(expiresAt)
                .build();

        fileShareRepository.save(share);
    }

    public void removeFilePermission(Long fileId, Long sharedWithUserId) {
        fileShareRepository.deleteAll(
                fileShareRepository.findAll().stream()
                        .filter(s -> s.getFile().getId().equals(fileId)
                                && s.getSharedWith().getId().equals(sharedWithUserId))
                        .toList()
        );
    }

    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        // Check user permission (optional, as you have it)
        FileRecord file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        // Delete related file shares first
        fileShareRepository.deleteByFileId(fileId);

        // Now delete the file itself
        fileRepository.deleteById(fileId);
    }
}
