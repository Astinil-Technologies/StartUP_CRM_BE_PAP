package startup.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final String UPLOAD_DIR = "uploads/reminders/";

    @PostMapping
    public ResponseEntity<String> uploadAttachment(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDirPath = System.getProperty("user.dir") + "/uploads/reminders/";
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String sanitizedFilename = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            String filename = UUID.randomUUID() + "_" + sanitizedFilename;
            String filePath = uploadDirPath + filename;

            System.out.println("Uploading file to: " + filePath);

            file.transferTo(new File(filePath));

            return ResponseEntity.ok("/uploads/reminders/" + filename);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String filename) {
        System.out.println("Download request for file: " + filename);
        try {
            Path filePath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR, filename);
            System.out.println("Resolved file path: " + filePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                System.out.println("File does not exist.");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}