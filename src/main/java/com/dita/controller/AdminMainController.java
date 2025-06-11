// src/main/java/com/dita/controller/AdminMainController.java
package com.dita.controller;

import com.dita.persistence.MemberRepository;
import com.dita.persistence.ReportRepository;
import com.dita.persistence.RestaurantRepository;
import com.dita.domain.Inquiry.InquiryStatus;
import com.dita.persistence.InquiryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.dita.domain.ReportStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AdminMainController {

	private final MemberRepository memberRepo;
	private final RestaurantRepository restaurantRepo;
	private final InquiryRepository inquiryRepo;
	private final ReportRepository reportRepo;

	public AdminMainController(MemberRepository memberRepo, RestaurantRepository restaurantRepo,
			InquiryRepository inquiryRepo,ReportRepository reportRepo) {
		this.memberRepo = memberRepo;
		this.restaurantRepo = restaurantRepo;
		this.inquiryRepo = inquiryRepo;
		this.reportRepo = reportRepo;
	}

	/** 대시보드 진입 시 → GET /admin */
	@GetMapping("/admin")
	public String adminMainPage(Model model) {
		// 1) 전체 회원 수
		long userCount = memberRepo.count();
		model.addAttribute("userCount", userCount);

		// 2) 최신 가입 회원
		memberRepo.findTopByOrderByCreateAtDesc().ifPresent(u -> model.addAttribute("latestUser", u));

		// 3) 대기 중인 가게 수
		long pendingCount = restaurantRepo.countByStatus("대기");
		model.addAttribute("pendingCount", pendingCount);

		// 4) 대기 중인 가게 중 최신 등록 한 건
		restaurantRepo.findTopByStatusOrderByCreatedAtDesc("대기")
				.ifPresent(store -> model.addAttribute("latestStore", store));

		// 1) 문의 개수 (대기 상태)
        long inquiryCount = inquiryRepo.countByStatus(InquiryStatus.대기중);
        model.addAttribute("inquiryCount", inquiryCount);

        // 2) 최신 대기 문의 1건
        inquiryRepo.findTopByStatusOrderByCreatedAtDesc(InquiryStatus.대기중)
                   .ifPresent(inq -> model.addAttribute("latestInquiry", inq));

        // 3) 대기 중인 신고 개수
        long reportCount = reportRepo.countByStatus(ReportStatus.대기);
        model.addAttribute("reportCount", reportCount);

        // 4) 최신 대기 중인 신고
        reportRepo.findTopByStatusOrderByReportedAtDesc(ReportStatus.대기)
                .ifPresent(report -> model.addAttribute("latestReport", report));
        
        
		return "admin/adminMain";
	}

	/** AJAX 로 숫자만 새로고침 → GET /admin/dashboard/refresh */
	@GetMapping("/admin/dashboard/refresh")
	@ResponseBody
	public Map<String, Long> refreshDashboardData() {
		// 사용자 수 + 대기 중인 가게 수 둘 다 리턴
		return Map.of("userCount", memberRepo.count(), "pendingCount", restaurantRepo.countByStatus("대기"),"reportCount", reportRepo.countByStatus(ReportStatus.대기));
	}

}
