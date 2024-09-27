package com.example.deliciousBee.model.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantReportRequest {
    private Long restaurantId;
    private String reporterId; // 옵션: 신고자 정보
    private String reportContent;
}
