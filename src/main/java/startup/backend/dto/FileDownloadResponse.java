package startup.backend.dto;
public class FileDownloadResponse {
    private final String fileName;
    private final byte[] content;
    private final String mediaType;

    public FileDownloadResponse(String fileName, byte[] content, String mediaType) {
        this.fileName = fileName;
        this.content = content;
        this.mediaType = mediaType;
    }

    public String getFileName() { return fileName; }
    public byte[] getContent() { return content; }
    public String getMediaType() { return mediaType; }
}


