package com.example.deliciousBee.repository;
import com.example.deliciousBee.dto.message.Message;
import com.example.deliciousBee.model.member.BeeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 특정 유저가 받은 메시지 조회
    List<Message> findByReceiverOrderByTimestampDesc(BeeMember receiver);

    // 특정 유저가 보낸 메시지 조회
    List<Message> findBySenderOrderByTimestampDesc(BeeMember sender);

    // 두 유저 간의 대화 조회
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversationBetweenUsers(BeeMember user1, BeeMember user2);
}
