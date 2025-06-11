package com.dita.controller;

import com.dita.dto.AdminDTO;
import com.dita.dto.AnnouncementDTO;
import com.dita.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminNoticeController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 공지 목록 화면
     *
     * @param page    페이지 번호 (0부터 시작)
     * @param size    페이지 크기
     * @param type    공지 유형 필터 (빈 문자열 또는 null이면 전체)
     * @param keyword 검색 키워드 (빈 문자열 또는 null이면 검색 무시)
     */
    @GetMapping("/adminNotices")
    public String adminNoticePage(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        // ─── 사이드바 활성화 플래그 (생략 가능) ───
        model.addAttribute("isDashboard", false);
        model.addAttribute("isUserManagement", false);
        model.addAttribute("isRestaurantManagement", false);
        model.addAttribute("isCourseRegister", false);
        model.addAttribute("isEventManagement", false);
        model.addAttribute("isInquiryManagement", false);
        model.addAttribute("isReportManagement", false);
        model.addAttribute("isNoticeManagement", true);

        // 1) 빈 문자열로 넘어온 경우 null로 처리
        if (type != null && type.trim().isEmpty()) {
            type = null;
        }
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // 2) Service에 page, size, type, keyword 전달
        Page<AnnouncementDTO> announcementPage =
                announcementService.getPagedAnnouncements(page, size, type, keyword);

        // 3) 뷰에 필요한 데이터 추가
        model.addAttribute("noticeList", announcementPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", announcementPage.getTotalPages());
        // 셀렉트 박스와 검색창에 기존 값 유지
        model.addAttribute("selectedType", type == null ? "" : type);
        model.addAttribute("keyword",      keyword == null ? "" : keyword);

        return "admin/adminNotices";
    }

    /**
     * 공지 상세 페이지
     */
    @GetMapping("/adminNoticeDetail/{id}")
    public String adminNoticeDetail(@PathVariable("id") Long noticeId, Model model) {
        // ─── 사이드바 활성화 플래그 (생략 가능) ───
        model.addAttribute("isDashboard", false);
        model.addAttribute("isUserManagement", false);
        model.addAttribute("isRestaurantManagement", false);
        model.addAttribute("isCourseRegister", false);
        model.addAttribute("isEventManagement", false);
        model.addAttribute("isInquiryManagement", false);
        model.addAttribute("isReportManagement", false);
        model.addAttribute("isNoticeManagement", true);

        AnnouncementDTO notice = announcementService.getAnnouncementById(noticeId);
        if (notice == null) {
            return "redirect:/adminNotices";
        }
        model.addAttribute("notice", notice);
        return "admin/adminNoticeDetail";
    }

    /**
     * 공지 작성/수정 페이지
     */
    @GetMapping("/adminNoticeWrite")
    public String adminNoticeWritePage(Model model, @RequestParam(required = false) Long id) {
        // ─── 사이드바 활성화 플래그 (생략 가능) ───
        model.addAttribute("isDashboard", false);
        model.addAttribute("isUserManagement", false);
        model.addAttribute("isRestaurantManagement", false);
        model.addAttribute("isCourseRegister", false);
        model.addAttribute("isEventManagement", false);
        model.addAttribute("isInquiryManagement", false);
        model.addAttribute("isReportManagement", false);
        model.addAttribute("isNoticeManagement", true);

        if (id != null) {
            AnnouncementDTO announcement = announcementService.getAnnouncementById(id);
            if (announcement != null) {
                model.addAttribute("notice", announcement);
                model.addAttribute("isEdit", true);
            }
        } else {
            model.addAttribute("isEdit", false);
        }

        return "admin/adminNoticeWrite";
    }

    /**
     * 공지 저장 (신규/수정)
     */
    @PostMapping("/adminNoticeWrite")
    public String saveNotice(
            @ModelAttribute AnnouncementDTO announcementDTO,
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {
        // 1) 업로드된 파일을 DTO에 세팅
        announcementDTO.setFile(file);

        // 2) 세션에서 AdminDTO 꺼내와 adminId 설정
        AdminDTO loggedInAdmin = (AdminDTO) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin != null) {
            announcementDTO.setAdminId(loggedInAdmin.getAdminId());
        } else {
            // 세션에 관리자 정보가 없으면 로그인 화면으로 리다이렉트
            return "redirect:/login";
        }

        // 3) 서비스 호출하여 저장 (신규/수정 모두 처리)
        announcementService.saveAnnouncement(announcementDTO);
        return "redirect:/adminNotices";
    }

    /**
     * 공지 삭제
     */
    @PostMapping("/adminNoticeDelete")
    public String deleteNotice(@RequestParam Long id) {
        try {
            announcementService.deleteAnnouncement(id);
        } catch (Exception e) {
            // 삭제 실패 시에도 단순 리다이렉트 처리
        }
        return "redirect:/adminNotices";
    }
    
    @GetMapping("/notices")
    public String showNoticeList(Model model) {
        // AnnouncementService 에 이미 getAllAnnouncements()가 "최신순 전체 공지"를 반환하고 있으므로
        List<AnnouncementDTO> allNotices = announcementService.getAllAnnouncements();

        // 뷰에서 ${notices} 로 반복할 수 있도록 Model 에 넘긴다.
        model.addAttribute("notices", allNotices);
        return "notice/noticeList";  // → src/main/resources/templates/notice/noticeList.html
    }
    
    
}
