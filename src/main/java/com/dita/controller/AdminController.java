// src/main/java/com/dita/controller/AdminController.java
package com.dita.controller;

import com.dita.dto.AdminDTO;
import com.dita.domain.Member;
import com.dita.domain.Inquiry;
import com.dita.domain.ReportStatus;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.RestaurantRepository;
import com.dita.persistence.ReportRepository;
import com.dita.persistence.InquiryRepository; // 추가
import com.dita.domain.Inquiry; // 추가
import com.dita.domain.Inquiry.InquiryStatus; // 추가
import com.dita.persistence.InquiryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final MemberRepository memberRepo;
	private final RestaurantRepository restaurantRepo;
	private final InquiryRepository inquiryRepo; // 추가
	private final ReportRepository reportRepo;
	private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public AdminController(MemberRepository memberRepo, RestaurantRepository restaurantRepo,
			InquiryRepository inquiryRepo,ReportRepository reportRepo	) {
		this.memberRepo = memberRepo;
		this.restaurantRepo = restaurantRepo;
		this.inquiryRepo = inquiryRepo;
		this.reportRepo = reportRepo;
	}

	@GetMapping("/adminMain")
	public String adminMain(HttpSession session, Model model) {
		AdminDTO adminDto = (AdminDTO) session.getAttribute("loggedInAdmin");
		if (adminDto == null) {
			return "redirect:/login";
		}
		model.addAttribute("admin", adminDto);

		// ── 사용자 관리
		long userCount = memberRepo.count();
		model.addAttribute("userCount", userCount);
		memberRepo.findTopByOrderByCreateAtDesc().ifPresent(m -> {
			model.addAttribute("latestUser", m);
			model.addAttribute("latestUserDate", m.getCreateAt().format(DATE_FMT));
		});

		// ── 가게 승인/관리 (대기 중)
		long pendingCount = restaurantRepo.countByStatus("대기");
		model.addAttribute("pendingCount", pendingCount);
		restaurantRepo.findTopByStatusOrderByCreatedAtDesc("대기").ifPresent(r -> {
			model.addAttribute("latestStore", r);
			model.addAttribute("latestStoreDate", r.getCreatedAt().format(DATE_FMT));
		});

		// 3) 문의 관리 (대기중)
		long inquiryCount = inquiryRepo.countByStatus(Inquiry.InquiryStatus.대기중);
		model.addAttribute("inquiryCount", inquiryCount);
		inquiryRepo.findTopByStatusOrderByCreatedAtDesc(Inquiry.InquiryStatus.대기중).ifPresent(i -> {
			model.addAttribute("latestInquiry", i);
			model.addAttribute("latestInquiryDate", i.getCreatedAt().format(DATE_FMT));
		});
		
		// ── 대기 중인 신고 개수
        long reportCount = reportRepo.countByStatus(ReportStatus.대기);  // 신고 개수
        model.addAttribute("reportCount", reportCount);
        reportRepo.findTopByStatusOrderByReportedAtDesc(ReportStatus.대기)
                .ifPresent(report -> model.addAttribute("latestReport", report));

		return "admin/adminMain";
	}

	// ... (다른 @GetMapping 메서드들)
}
