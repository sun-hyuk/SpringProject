package com.dita.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dita.dto.AnnouncementDTO;
import com.dita.service.AnnouncementService;

@Controller
public class NoticeController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * GET /notice
     *   - 사용자용 공지사항 목록 페이지
     */
    @GetMapping("/notice")
    public String showNoticeList(Model model) {
        List<AnnouncementDTO> allNotices = announcementService.getAllAnnouncements();
        model.addAttribute("notices", allNotices);
        return "notice/notice";
    }

    /**
     * GET /notice/{id}
     *   - 공지 상세 페이지
     */
    @GetMapping("/notice/{id}")
    public String showNoticeDetail(
            @PathVariable("id") Long announcementId,
            Model model
    ) {
        AnnouncementDTO dto = announcementService.getAnnouncementById(announcementId);
        if (dto == null) {
            return "redirect:/notice";  // 없는 공지라면 목록으로 리다이렉트
        }
        model.addAttribute("notice", dto);

        // (선택) 당첨자 명단이 있다면 winners 라는 List 객체를 같이 넘겨줍니다.
        // 예:
        // List<Winner> winnersList = winnerService.findByAnnouncementId(announcementId);
        // model.addAttribute("winners", winnersList);

        return "notice/noticeDetail";
    }
}
