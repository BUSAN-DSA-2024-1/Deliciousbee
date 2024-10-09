package com.example.deliciousBee.service.message;
import com.example.deliciousBee.dto.message.Message;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.repository.BeeMemberRepository;
import com.example.deliciousBee.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final BeeMemberRepository beeMemberRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository, BeeMemberRepository beeMemberRepository) {
        this.messageRepository = messageRepository;
        this.beeMemberRepository = beeMemberRepository;
    }

    // 메시지 전송
    @Transactional
    public Message sendMessage(String senderNickname, String receiverNickname, String content) {
        BeeMember sender = beeMemberRepository.findByNickname(senderNickname)
                .orElseThrow(() -> new RuntimeException("발신자 없음: " + senderNickname));
        BeeMember receiver = beeMemberRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverNickname));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message ReportMessage(String receiverNickname, String content) {
        BeeMember sender = beeMemberRepository.findByNickname("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: admin"));
        BeeMember receiver = beeMemberRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverNickname));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message ReviewReportSubmitMessage(String receiverNickname) {
        String content = "리뷰 신고가 검토 후 정상적으로 처리되었습니다.";

        BeeMember sender = beeMemberRepository.findByNickname("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: admin"));
        BeeMember receiver = beeMemberRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverNickname));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message RestaurantReportSubmitMessage(String receiverNickname) {
        String content = "레스토랑 등록이 검토 후 정상적으로 처리되었습니다.";

        BeeMember sender = beeMemberRepository.findByNickname("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: admin"));
        BeeMember receiver = beeMemberRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverNickname));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message RestaurantReportMessage(String receiverNickname) {
        String content = "레스토랑 등록이 정상적으로 요청되었습니다.";

        BeeMember sender = beeMemberRepository.findByNickname("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: admin"));
        BeeMember receiver = beeMemberRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverNickname));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    // 받은 메시지 조회
    @Transactional(readOnly = true)
    public List<Message> getReceivedMessages(String receiverNickname) {
        BeeMember receiver = beeMemberRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + receiverNickname));
        return messageRepository.findByReceiverOrderByTimestampDesc(receiver);
    }

    // 보낸 메시지 조회
    @Transactional(readOnly = true)
    public List<Message> getSentMessages(String senderNickname) {
        BeeMember sender = beeMemberRepository.findByNickname(senderNickname)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + senderNickname));
        return messageRepository.findBySenderOrderByTimestampDesc(sender);
    }

    // 두 유저 간의 대화 조회
    @Transactional(readOnly = true)
    public List<Message> getConversation(String nickname1, String nickname2) {
        BeeMember user1 = beeMemberRepository.findByNickname(nickname1)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + nickname1));
        BeeMember user2 = beeMemberRepository.findByNickname(nickname2)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + nickname2));
        return messageRepository.findConversationBetweenUsers(user1, user2);
    }
}
