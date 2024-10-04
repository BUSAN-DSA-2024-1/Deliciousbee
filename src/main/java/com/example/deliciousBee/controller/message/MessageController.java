package com.example.deliciousBee.controller.message;

import com.example.deliciousBee.dto.message.Message;
import com.example.deliciousBee.dto.message.MessageRequest;
import com.example.deliciousBee.dto.message.MessageResponse;
import com.example.deliciousBee.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

        // 메시지 전송
        @PostMapping("/send")
        public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest messageRequest) {
            Message message = messageService.sendMessage(
                    messageRequest.getSenderId(),
                    messageRequest.getReceiverId(),
                    messageRequest.getContent()
            );
            return new ResponseEntity<>(new MessageResponse(message), HttpStatus.CREATED);
        }

    // 받은 메시지 조회
    @GetMapping("/received/{receiverId}")
    public ResponseEntity<List<MessageResponse>> getReceivedMessages(@PathVariable String receiverId) {
        List<Message> messages = messageService.getReceivedMessages(receiverId);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .toList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 보낸 메시지 조회
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<MessageResponse>> getSentMessages(@PathVariable String senderId) {
        List<Message> messages = messageService.getSentMessages(senderId);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .toList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 두 유저 간의 대화 조회
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestParam String userId1,
            @RequestParam String userId2) {
        List<Message> messages = messageService.getConversation(userId1, userId2);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .toList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
