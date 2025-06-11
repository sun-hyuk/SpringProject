package com.dita.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dita.domain.Member;
import com.dita.domain.Alerts;
import com.dita.domain.Report;
import com.dita.domain.ReportStatus;
import com.dita.domain.ReportType;
import com.dita.domain.Review;
import com.dita.dto.ReportListDTO;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.ReportRepository;
import com.dita.persistence.ReviewRepository;

import jakarta.transaction.Transactional;

@Service
public class ReportService {

	@Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReviewRepository reviewRepository; // 신고 대상 사용자 조회용

    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private AlertsService alertsService;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(int id) {
        return reportRepository.findById(id).orElse(null);
    }

    public void saveReport(Report report) {
        reportRepository.save(report);
    }

    public void deleteReport(int id) {
        reportRepository.deleteById(id);
    }

    public Page<ReportListDTO> getAllReports(Pageable pageable) {
        return reportRepository.findReportList(pageable);
    }

    
    public Page<ReportListDTO> getFilteredReports(ReportStatus status, ReportType type, String keyword, Pageable pageable) {
        return reportRepository.findFilteredReportDTOs(status, type, keyword, pageable);
    }
    
    @Transactional
    public void insertReviewReport(String reporterId, int reviewId, String reason) {
        Member reporter = memberRepository.findById(reporterId).orElseThrow();
        Review review = reviewRepository.findById(reviewId)
        	    .orElseThrow(() -> new NoSuchElementException("해당 리뷰 ID(" + reviewId + ")가 존재하지 않습니다."));
        Member reported = review.getMember(); // 리뷰 작성자

        Report report = new Report();
        report.setType(ReportType.리뷰);
        report.setTargetId(reviewId);
        report.setReason(reason);
        report.setStatus(ReportStatus.대기);
        report.setReporterMember(reporter);
        report.setReportedMember(reported);

        reportRepository.save(report);
        
        // 🚨 1. 신고자에게 "신고가 접수되었습니다"
        alertsService.createAlert(
            reporter.getMemberId(),
            Alerts.AlertType.신고,
            reviewId
        );

        // 🚨 2. 피신고자에게 "해당 리뷰가 신고되었습니다"
        alertsService.createAlert(
            reported.getMemberId(),
            Alerts.AlertType.신고,
            reviewId
        );
    }
    
    @Transactional
    public void completeReport(Integer reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("신고 내역이 존재하지 않습니다."));

        report.setStatus(ReportStatus.완료);  // 상태 변경
        reportRepository.save(report);

        // 신고자에게 "신고가 처리되었습니다" 알림
        alertsService.createAlert(
            report.getReporterMember().getMemberId(),   // 수신자
            Alerts.AlertType.신고,                      // 알림 유형
            report.getReportId()                        // targetId: 신고 ID
        );
    }
    
    // 로그인한 사용자의 신고 목록 조회
    public List<ReportListDTO> getReportListByMemberId(String memberId) {
        return reportRepository.findAllByReporterId(memberId);
    }
}
