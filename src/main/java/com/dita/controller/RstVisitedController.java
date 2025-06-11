package com.dita.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dita.dto.MemberDTO;
import com.dita.dto.RstVisitedDTO;  // 변경된 DTO import
import com.dita.service.RstVisitedService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RstVisitedController {

    private final RstVisitedService visitedService;

    public RstVisitedController(RstVisitedService visitedService) {
        this.visitedService = visitedService;
    }

    @GetMapping("/rstVisited")
    public String showRecentVisited(Model model, HttpSession session) {
        // 1) 로그인된 회원 ID 꺼내기
        MemberDTO loginMember = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginMember != null) {
            String memberId = loginMember.getMemberId();
            // 2) Service 호출해서 RstVisitedDTO 리스트 가져오기
            List<RstVisitedDTO> recentVisitedViews = visitedService.getRecentVisits(memberId);
            // 3) 모델에 추가
            model.addAttribute("rstVisited", recentVisitedViews);
        }
        // 4) 뷰 이름 리턴 (src/main/resources/templates/rstVisited.html)
        return "rstVisited";
    }
}
