package com.example.deliciousBee.model.report;

import java.time.LocalDate;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.review.Review;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

@Entity
@Data
public class Report {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference // Report 엔티티가 "back" 참조
    private BeeMember beeMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    @JsonBackReference
    private Review review;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;
    
    @Column(name = "report_date")
    private LocalDate reportDate;


}
