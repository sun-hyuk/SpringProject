package com.dita.controller;

import com.dita.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FindAccountController {

    private final MemberService memberService;

    // 👉 아이디 찾기 폼 보여주기
    @GetMapping("/find-id")
    public String findIdPage() {
        return "login/find-id";
    }

    // 👉 아이디 찾기 처리
    @PostMapping("/find-id")
    public String processFindId(@RequestParam String name, @RequestParam String phone, Model model) {
        String maskedId = memberService.findMaskedMemberId(name, phone);
        model.addAttribute("memberId", maskedId);
        return "login/find-id-result";  // 결과 페이지 (아이디 마스킹 출력)
    }

    // 👉 비밀번호 재설정 폼 보여주기
    @GetMapping("/find-password")
    public String findPwdPage() {
        return "login/find-password";
    }

    // 👉 비밀번호 재설정 처리
    @PostMapping("/find-password")
    public String processFindPwd(@RequestParam String memberId, @RequestParam String phone, Model model) {
        boolean success = memberService.sendTemporaryPassword(memberId, phone);
        model.addAttribute("success", success);
        return "login/find-password-result";  // 성공/실패 메시지 출력
    }
}
