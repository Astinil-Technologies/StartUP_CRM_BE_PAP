package startup.backend.controller;

import startup.backend.dto.ChatMessage;
import startup.backend.dto.SignalMessage;
import startup.backend.dto.UserPresenceMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/signal/{roomId}")
    @SendTo("/topic/signal/{roomId}")
    public SignalMessage signal(@DestinationVariable String roomId, SignalMessage message) {
        return message;
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage chat(@DestinationVariable String roomId, ChatMessage message) {
        return message;
    }

    @MessageMapping("/presence/{roomId}")
    @SendTo("/topic/presence/{roomId}")
    public UserPresenceMessage presence(@DestinationVariable String roomId, UserPresenceMessage message) {
        return message;
    }
}
