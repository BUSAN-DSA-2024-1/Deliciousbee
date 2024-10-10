package com.example.deliciousBee.dto.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String senderNickname;
    private String receiverNickname;
    private String content;

    // getters and setters
}
