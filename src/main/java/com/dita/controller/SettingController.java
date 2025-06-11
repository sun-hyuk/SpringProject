// src/main/java/com/dita/controller/SettingController.java
package com.dita.controller;

import com.dita.dto.MemberDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingController {

    /**
     * 설정 및 활동 화면으로 이동합니다.
     * 로그인 상태를 확인하여 적절한 메뉴를 표시합니다.
     */
    @GetMapping("/setting")
    public String showSetting(HttpSession session, Model model) {
        // 세션에서 로그인 정보 가져오기
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        // 모델에 로그인 정보 추가
        model.addAttribute("loggedInMember", member);
        
        // 뷰 이름: templates/setting.html
        return "setting";
    }
}