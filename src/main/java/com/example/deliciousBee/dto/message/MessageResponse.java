package com.example.deliciousBee.dto.message;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponse {
    private Long id;
    private String senderNickname;
    private String receiverNickname;
    private String content;
    private LocalDateTime timestamp;

    public MessageResponse(Message message) {
        this.id = message.getId();
        this.senderNickname = message.getSender().getNickname();
        this.receiverNickname = message.getReceiver().getNickname();
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
    }

    // getters and setters
}

