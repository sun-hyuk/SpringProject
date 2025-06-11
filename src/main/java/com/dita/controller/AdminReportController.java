package com.dita.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dita.domain.Alerts;
import com.dita.domain.Member;
import com.dita.domain.Report;
import com.dita.domain.ReportStatus;
import com.dita.domain.ReportType;
import com.dita.domain.Review;
import com.dita.dto.ReportListDTO;
import com.dita.persistence.ReportRepository;
import com.dita.persistence.ReviewRepository;
import com.dita.service.AlertsService;

@Controller
@RequestMapping("/adminReports")
public class AdminReportController {

    private final ReviewRepository reviewRepository;

    private final ReportRepository reportRepository;
    
    private final AlertsService alertsService;

    public AdminReportController(ReportRepository reportRepository, ReviewRepository reviewRepository, AlertsService alertsService) {
        this.reportRepository = reportRepository;
        this.reviewRepository = reviewRepository;
        this.alertsService = alertsService;
    }

    // 목록 조회
    @GetMapping("")
    public String listReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "reportId") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        // status 문자열을 ReportStatus enum으로 변환
        ReportStatus statusEnum = null;
        if (status != null && !status.equals("전체 상태")) {
            try {
                statusEnum = ReportStatus.valueOf(status); // "대기", "완료"
            } catch (IllegalArgumentException e) {
                statusEnum = null; // 잘못된 값일 경우 무시
            }
        }

        // type 문자열을 ReportType enum으로 변환
        ReportType typeEnum = null;
        if (type != null && !type.equals("전체 유형")) {
            try {
                typeEnum = ReportType.valueOf(type); // "게시글", "댓글", "리뷰"
            } catch (IllegalArgumentException e) {
                typeEnum = null;
            }
        }

        Pageable pageable = PageRequest.of(page - 1, 8, Sort.by(Sort.Direction.fromString(order), sort));
        Page<ReportListDTO> reportPage = reportRepository.findFilteredReportDTOs(
            statusEnum, typeEnum, keyword, pageable);

        model.addAttribute("reports", reportPage.getContent());
        model.addAttribute("totalCount", reportPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("startRow", (page - 1) * 8);

        // 필터 값 유지용
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        return "admin/adminReports";
    }


    // 상세 보기
    @GetMapping("/detail/{id}")
    public String reportDetail(@PathVariable Integer id, Model model) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다."));

        model.addAttribute("report", report);

        // 신고 유형이 리뷰인 경우 review 테이블에서 신고 대상 content 조회
        if (report.getType() == ReportType.리뷰) {
            Integer reviewId = report.getTargetId();  // 수정된 필드명 반영
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            reviewOpt.ifPresent(review -> model.addAttribute("reviewContent", review.getContent()));
        }

        return "admin/adminReportsDetail";
    }

    @PostMapping("/{id}/process")
    @ResponseBody
    public Map<String, Object> processReport(@PathVariable Integer id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다."));

        if (report.getStatus() != ReportStatus.대기) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        report.setStatus(ReportStatus.완료);
        reportRepository.save(report);

        // ✅ 신고자에게 "신고가 처리되었습니다"
        alertsService.createAlert(
            report.getReporterMember().getMemberId(),
            Alerts.AlertType.신고,
            report.getReportId()  // 신고 ID
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "신고가 성공적으로 처리되었습니다.");
        return result;
    }



    // 신고 삭제
    @DeleteMapping("/{id}")
    @ResponseBody
    public Map<String, Object> deleteReport(@PathVariable Integer id) {
    	reportRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "신고가 삭제되었습니다.");
        return result;
    }
    
    @PostMapping("/reject/{id}")
    @ResponseBody
    public Map<String, Object> rejectReport(@PathVariable Integer id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("신고 없음"));

        if (report.getStatus() == ReportStatus.완료 || report.getStatus() == ReportStatus.거절) {
            Map<String, Object> already = new HashMap<>();
            already.put("success", false);
            already.put("message", "이미 처리된 신고입니다.");
            return already;
        }

        report.setStatus(ReportStatus.거절);
        reportRepository.save(report);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "신고가 거절 처리되었습니다.");
        return result;
    }

}
