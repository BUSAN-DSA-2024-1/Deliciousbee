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
    public Message sendMessage(String senderId, String receiverId, String content) {



        BeeMember sender = beeMemberRepository.findByMemberid(senderId)
                .orElseThrow(() -> new RuntimeException("발신자 없음: " + senderId));
        BeeMember receiver = beeMemberRepository.findByMemberid(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverId));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message ReviewReportMessage(String receiverId) {


        String content ="리뷰 신고가 접수되었습니다.";

        BeeMember sender = beeMemberRepository.findByMemberid("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: " + "admin"));
        BeeMember receiver = beeMemberRepository.findByMemberid(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverId));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message ReviewReportSubmitMessage(String receiverId) {


        String content ="리뷰 신고가 검토후 정상적으로 처리되었습니다.";

        BeeMember sender = beeMemberRepository.findByMemberid("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: " + "admin"));
        BeeMember receiver = beeMemberRepository.findByMemberid(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverId));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Message RestaurantReportSubmitMessage(String receiverId) {


        String content ="레스토랑 등록이 검토후 정상적으로 처리되었습니다.";

        BeeMember sender = beeMemberRepository.findByMemberid("admin")
                .orElseThrow(() -> new RuntimeException("발신자 없음: " + "admin"));
        BeeMember receiver = beeMemberRepository.findByMemberid(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverId));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }
    @Transactional
    public Message RestaurantReportMessage(String receiverId) {


        String content ="레스토랑 등록이 정상적으로 요청되었습니다.";

        BeeMember sender = beeMemberRepository.findByMemberid("1234")
                .orElseThrow(() -> new RuntimeException("발신자 없음: " + "1234"));
        BeeMember receiver = beeMemberRepository.findByMemberid(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자 없음: " + receiverId));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepository.save(message);
    }


    // 받은 메시지 조회
    @Transactional(readOnly = true)
    public List<Message> getReceivedMessages(String receiverId) {


        Optional<BeeMember> recoco = beeMemberRepository.findByMemberid(receiverId);
        System.out.println(recoco);
        BeeMember receiver = beeMemberRepository.findByMemberid(receiverId)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + receiverId));
        return messageRepository.findByReceiverOrderByTimestampDesc(receiver);
    }

    // 보낸 메시지 조회
    @Transactional(readOnly = true)
    public List<Message> getSentMessages(String senderId) {
        BeeMember sender = beeMemberRepository.findByMemberid(senderId)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + senderId));
        return messageRepository.findBySenderOrderByTimestampDesc(sender);
    }

    // 두 유저 간의 대화 조회
    @Transactional(readOnly = true)
    public List<Message> getConversation(String userId1, String userId2) {
        BeeMember user1 = beeMemberRepository.findByMemberid(userId1)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + userId1));
        BeeMember user2 = beeMemberRepository.findByMemberid(userId2)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + userId2));
        return messageRepository.findConversationBetweenUsers(user1, user2);
    }
}
