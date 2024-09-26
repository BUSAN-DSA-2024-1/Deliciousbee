package com.example.deliciousBee.model.report;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RestaurantReportDTO {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String reporterId;
    private String reporterName;
    private String reportContent;
    private LocalDateTime reportDate;
    private RestaurantReport.ReportStatus status;

    // 생성자 또는 빌더 패턴을 통해 엔티티를 DTO로 변환
    public RestaurantReportDTO(RestaurantReport report) {
        this.id = report.getId();
        this.restaurantId = report.getRestaurant().getId();
        this.restaurantName = report.getRestaurant().getName();
        if (report.getReporter() != null) {
            this.reporterId = report.getReporter().getMember_id();
            this.reporterName = report.getReporter().getName();
        }
        this.reportContent = report.getReportContent();
        this.reportDate = report.getReportDate();
        this.status = report.getStatus();
    }
}
