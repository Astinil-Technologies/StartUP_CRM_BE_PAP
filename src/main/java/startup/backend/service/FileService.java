package startup.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import startup.backend.dto.FileDto;
import startup.backend.dto.FileDownloadResponse;
import startup.backend.entity.FileRecord;
import startup.backend.entity.User;
import startup.backend.repository.FileRepository;
import startup.backend.repository.FileShareRepository;
import startup.backend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileShareRepository fileShareRepository;  

    public void uploadByUsername(String fileName, String type, Long size, MultipartFile file,
                                 String sharedByUsername, String receiverUsername) throws IOException {
        User sharedBy = userRepository.findByUsername(sharedByUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        FileRecord record = FileRecord.builder()
                .name(fileName)
                .type(type)
                .size(size)
                .fileData(file.getBytes())
                .sharedBy(sharedBy)
                .receiver(receiver)
                .uploadedAt(LocalDateTime.now())
                .build();

        fileRepository.save(record);
    }


    public Page<FileDto> getReceivedFiles(Long userId, Pageable pageable) {
        return fileRepository.fetchReceivedLight(userId, pageable);
    }

    public FileDownloadResponse previewFileWithType(Long fileId, Long userId) {
        FileRecord file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("❌ File not found: ID = " + fileId));

        if (!file.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("❌ Unauthorized access for userId = " + userId);
        }

        byte[] content = file.getFileData();
        if (content == null || content.length == 0) {
            throw new RuntimeException("❌ File content is empty for fileId = " + fileId);
        }

        String extension = file.getType();

        String mediaType = detectMimeType(content, extension);


        return new FileDownloadResponse(file.getName(), content, mediaType);
    }


    public FileDownloadResponse downloadFileWithMetadata(Long fileId, Long userId) {
        FileRecord file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("❌ File not found: " + fileId));

        if (!file.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("❌ Unauthorized access for userId: " + userId);
        }

        byte[] content = file.getFileData();
        if (content == null || content.length == 0) {
            throw new RuntimeException("❌ File content is missing or empty");
        }

        String extension = file.getType();
        if (extension == null || extension.isBlank()) {
            extension = "bin";
        }



        String mediaType = detectMimeType(content, extension);


        return new FileDownloadResponse(file.getName(), content, mediaType);
    }




    private String detectMimeType(byte[] content, String extension) {
        String mediaType = "application/octet-stream";

        try {
            // ✅ Clean and sanitize the extension
            String safeExt = (extension != null && extension.matches("^[a-zA-Z0-9]+$")) ? extension : "bin";

            Path tempFile = Files.createTempFile("file-", "." + safeExt);
            Files.write(tempFile, content);

            String detected = Files.probeContentType(tempFile);
            if (detected != null && !detected.isBlank()) {
                mediaType = detected;
            }

            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace(); // Still log if something else goes wrong
        }

        return mediaType;
    }


















    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        // First, delete any records in the file_share table that reference this file
        fileShareRepository.deleteByFileId(fileId);  // This method should be implemented in FileShareRepository

        // Then, delete the file from the files table
        fileRepository.deleteById(fileId);
    }



}
