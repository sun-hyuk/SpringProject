package com.dita.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dita.domain.Alerts;
import com.dita.domain.Report;
import com.dita.dto.MemberDTO;
import com.dita.persistence.ReportRepository;
import com.dita.service.AlertsService;
import com.dita.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final AlertsService alertsService;

    @Autowired
    public ReportController(ReportService reportService, ReportRepository reportRepository, AlertsService alertsService) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.alertsService = alertsService;
    }

    // 사용자 신고 내역 페이지
    @GetMapping("")
    public String reportList(HttpSession session, Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");

        if (memberDto != null) {
            model.addAttribute("loggedInMember", memberDto);
            model.addAttribute("username", memberDto.getNickname());

            // 신고 리스트 조회 추가
            model.addAttribute("reportList", reportService.getReportListByMemberId(memberDto.getMemberId()));

        } else {
            model.addAttribute("loggedInMember", null);
            model.addAttribute("username", "비회원");

            // 비회원은 빈 리스트라도 보내야 오류 없음
            model.addAttribute("reportList", new java.util.ArrayList<>());
        }

        return "report";
    }

    // 신고 작성 폼
    @GetMapping("/write")
    public String reportWritePage() {
        return "report-write";
    }

    // 리뷰 신고 API
    @PostMapping("/review/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> reportReview(
            @PathVariable int reviewId,
            @RequestParam String reason,
            HttpServletRequest request) {

        MemberDTO memberDto = (MemberDTO) request.getSession().getAttribute("loggedInMember");
        if (memberDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String reporterId = memberDto.getMemberId();
        reportService.insertReviewReport(reporterId, reviewId, reason);

		/*
		 * // 🔔 알림 추가: 신고자에게 "신고가 접수되었습니다" 알림 전송 alertsService.createAlert( reporterId,
		 * Alerts.AlertType.신고, reviewId // 신고 접수 알림은 targetId 없이 처리 );
		 */

        return ResponseEntity.ok("신고가 접수되었습니다.");
    }
    
    // 신고 상세 조회 (JSON)
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getReportDetail(@PathVariable Integer id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("해당 신고가 존재하지 않습니다."));

        // 상세 내용 DTO나 Map 반환
        Map<String, String> data = new HashMap<>();
        data.put("type", report.getType().name()); // 예: 댓글, 리뷰
        data.put("author", report.getReportedMember().getNickname());
        data.put("reporter", report.getReporterMember().getNickname());
        data.put("date", report.getReportedAt().toString()); // 또는 포맷팅
        data.put("content", report.getReason());
        data.put("result", report.getStatus().name()); // 대기/완료/거절 등

        return ResponseEntity.ok(data);
    }

    // 상태별 탭
    @GetMapping("/{status}")
    public String reportByStatus(@PathVariable String status, Model model) {
        model.addAttribute("activeTab", status); // 예: received, confirmed 등
        return "report";
    }
}
