package com.example.deliciousBee.dto.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String senderId;
    private String receiverId;
    private String content;
}
