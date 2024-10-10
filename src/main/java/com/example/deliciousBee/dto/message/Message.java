package com.example.deliciousBee.dto.message;
import com.example.deliciousBee.model.member.BeeMember;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 발신자
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)  // 연쇄 삭제 추가
    @JoinColumn(name = "sender_id", nullable = false)
    private BeeMember sender;

    // 수신자
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)  // 연쇄 삭제 추가
    @JoinColumn(name = "receiver_id", nullable = false)
    private BeeMember receiver;

    // 메시지 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 전송 시간
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // 생성 시점에 timestamp 설정
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
