package startup.backend.dto;

public class SignalMessage {
    private String type;
    private String roomId;
    private String data;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}