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
import java.util.stream.Collectors;

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
                messageRequest.getSenderNickname(),
                messageRequest.getReceiverNickname(),
                messageRequest.getContent()
        );
        return new ResponseEntity<>(new MessageResponse(message), HttpStatus.CREATED);
    }

    // 받은 메시지 조회
    @GetMapping("/received/{receiverNickname}")
    public ResponseEntity<List<MessageResponse>> getReceivedMessages(@PathVariable String receiverNickname) {
        List<Message> messages = messageService.getReceivedMessages(receiverNickname);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 보낸 메시지 조회
    @GetMapping("/sent/{senderNickname}")
    public ResponseEntity<List<MessageResponse>> getSentMessages(@PathVariable String senderNickname) {
        List<Message> messages = messageService.getSentMessages(senderNickname);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 두 유저 간의 대화 조회
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestParam String nickname1,
            @RequestParam String nickname2) {
        List<Message> messages = messageService.getConversation(nickname1, nickname2);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
