package com.example.deliciousBee.service.report;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.report.RestaurantReport;
import com.example.deliciousBee.repository.BeeMemberRepository;
import com.example.deliciousBee.repository.RestaurantReportRepository;
import com.example.deliciousBee.repository.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class RestaurantReportService {

    private final RestaurantReportRepository restaurantReportRepository;
    private final RestaurantRepository restaurantRepository;
    private final BeeMemberRepository beeMemberRepository;

    // 신고 생성
    @Transactional
    public RestaurantReport createReport(Long restaurantId, BeeMember reporterId, String reportContent) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found with id " + restaurantId));

        BeeMember reporter = reporterId;


        RestaurantReport report = new RestaurantReport(restaurant, reporter, reportContent);
        return restaurantReportRepository.save(report);
    }

    // 모든 신고 조회
    public List<RestaurantReport> getAllReports() {
        return restaurantReportRepository.findAll();
    }

    // 특정 레스토랑에 대한 신고 조회
    public List<RestaurantReport> getReportsByRestaurant(Long restaurantId) {
        return restaurantReportRepository.findByRestaurantId(restaurantId);
    }

    // 신고 상태 업데이트
    @Transactional
    public RestaurantReport updateReportStatus(Long reportId, RestaurantReport.ReportStatus status) {
        RestaurantReport report = restaurantReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found with id " + reportId));
        report.setStatus(status);
        return restaurantReportRepository.save(report);
    }

    // 신고 삭제
    @Transactional
    public void deleteReport(Long reportId) {
        if (!restaurantReportRepository.existsById(reportId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found with id " + reportId);
        }
        restaurantReportRepository.deleteById(reportId);
    }
}