package com.example.deliciousBee.repository;

import com.example.deliciousBee.model.report.RestaurantReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantReportRepository extends JpaRepository<RestaurantReport, Long> {

    // 특정 레스토랑에 대한 모든 신고 조회
    List<RestaurantReport> findByRestaurantId(Long restaurantId);

    // 특정 신고 상태의 신고 조회
    List<RestaurantReport> findByStatus(RestaurantReport.ReportStatus status);
}
