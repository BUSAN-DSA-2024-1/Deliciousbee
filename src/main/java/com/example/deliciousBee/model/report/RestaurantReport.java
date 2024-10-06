package com.example.deliciousBee.model.report;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.member.BeeMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
// RestaurantReport.java

import com.fasterxml.jackson.annotation.JsonBackReference;


@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "restaurant_reports")
public class RestaurantReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고된 레스토랑
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonBackReference // 순환 참조 방지
    private Restaurant restaurant;

    // 신고자 (옵션: 신고한 사용자 정보)
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    @JsonBackReference // 순환 참조 방지
    private BeeMember reporter;

    // 신고 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String reportContent;

    // 신고 날짜
    @Column(nullable = false)
    private LocalDateTime reportDate;

    // 신고 상태 (예: PENDING, RESOLVED, REJECTED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    // 신고 생성 시 날짜 자동 설정
    @PrePersist
    protected void onCreate() {
        this.reportDate = LocalDateTime.now().withNano(0); // 나노초 제거
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
    }

    public RestaurantReport(Restaurant restaurant, BeeMember reporter, String reportContent) {
        this.restaurant = restaurant;
        this.reporter = reporter;
        this.reportContent = reportContent;
        this.status = ReportStatus.PENDING;
    }

    // ReportStatus 열거형
    public enum ReportStatus {
        PENDING,
        RESOLVED,
        REJECTED
    }
}


