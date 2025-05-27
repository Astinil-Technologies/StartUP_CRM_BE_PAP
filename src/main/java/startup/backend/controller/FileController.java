package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import startup.backend.dto.FileDownloadResponse;
import startup.backend.dto.FileDto;
import startup.backend.enums.AccessType;
import startup.backend.service.FileService;
import startup.backend.service.FileShareService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileShareService fileShareService;

    @PostMapping("/upload-by-username")
    public ResponseEntity<String> uploadByUsername(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("receiverUsername") String receiverUsername,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String sharedByUsername = userDetails.getUsername();

            // Extract original name & extension
            String originalName = file.getOriginalFilename();
            String extension = "bin"; // fallback
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            }

            long size = file.getSize();

            // Save using safe, derived values
            fileService.uploadByUsername(originalName, extension, size, file, sharedByUsername, receiverUsername);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/received")
    public ResponseEntity<Page<FileDto>> getReceivedFiles(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return ResponseEntity.ok(fileService.getReceivedFiles(userId, pageable));
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long id, @RequestParam Long userId) {
        try {
            FileDownloadResponse response = fileService.previewFileWithType(id, userId);

            MediaType contentType;
            try {
                contentType = MediaType.parseMediaType(response.getMediaType());
            } catch (Exception e) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            String safeFilename = URLEncoder.encode(response.getFileName(), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + safeFilename)
                    .body(response.getContent());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Preview failed: " + e.getMessage()).getBytes());
        }
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, @RequestParam Long userId) {
        try {
            FileDownloadResponse response = fileService.downloadFileWithMetadata(id, userId);

            MediaType contentType;
            try {
                contentType = MediaType.parseMediaType(response.getMediaType());
            } catch (Exception e) {
                contentType = MediaType.APPLICATION_OCTET_STREAM; // fallback if MIME is invalid
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                    .body(response.getContent());

        } catch (Exception e) {
            e.printStackTrace(); // 🔍 Print actual error for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Download failed: " + e.getMessage()).getBytes());
        }
    }
    
    @PostMapping("/share")
    public ResponseEntity<?> shareFile(@RequestParam Long fileId,
                                       @RequestParam Long sharedWithUserId,
                                       @RequestParam AccessType accessType,
                                       @RequestParam(required = false) LocalDateTime expiresAt) {
        fileShareService.shareFile(fileId, sharedWithUserId, accessType, expiresAt);
        return ResponseEntity.ok("File shared successfully");
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId, @RequestParam Long userId) {
        try {
            fileService.deleteFile(fileId, userId);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Delete failed: " + e.getMessage());
        }
    }


}
