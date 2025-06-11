package com.dita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dita.dto.MemberDTO;

import jakarta.servlet.http.HttpSession;

@Controller
public class ServiceController {
    @GetMapping("/service")
    public String serviceMain(HttpSession session, Model model) {
		// 1) 세션에 "loggedInMember"가 있으면 MemberDTO를 꺼내고, 없으면 null
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        // 2) 뷰에 로그인 상태 확인용으로 넘겨둠
        model.addAttribute("loggedInMember", member);
        // 3) 항상 main.html을 반환 (로그인 여부와 상관없이 페이지가 열림)
        return "service";
    }
}