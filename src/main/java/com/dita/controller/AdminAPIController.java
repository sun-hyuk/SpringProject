package com.dita.controller;

import com.dita.domain.Report;
import com.dita.domain.ReportStatus;
import com.dita.persistence.ReportRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminAPIController {

    @Autowired
    private ReportRepository reportRepository;

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");

        try {
            ReportStatus newStatus = ReportStatus.valueOf(statusStr); // Enum 변환
            Optional<Report> optionalReport = reportRepository.findById(id);

            if (optionalReport.isEmpty()) {
                return ResponseEntity.badRequest().body("해당 신고를 찾을 수 없습니다.");
            }

            Report report = optionalReport.get();
            report.setStatus(newStatus);
            reportRepository.save(report);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 상태 값입니다.");
        }
    }
}
