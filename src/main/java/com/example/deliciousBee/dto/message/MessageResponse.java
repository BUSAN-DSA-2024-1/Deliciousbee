package com.example.deliciousBee.dto.message;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponse {
    private Long id;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;

    public MessageResponse(Message message) {
        this.id = message.getId();
        this.senderId = message.getSender().getMember_id();
        this.receiverId = message.getReceiver().getMember_id();
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
    }
}
