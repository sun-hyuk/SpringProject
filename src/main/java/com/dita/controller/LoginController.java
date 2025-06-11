package com.dita.controller;

import com.dita.dto.AdminDTO;
import com.dita.dto.MemberDTO;
import com.dita.persistence.MemberRepository;
import com.dita.service.AdminService;
import com.dita.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class LoginController {

    private final AdminService adminService;
    private final MemberService memberService;
    private final MemberRepository memberRepo;

    public LoginController(
            AdminService adminService,
            MemberService memberService,
            MemberRepository memberRepo) {
        this.adminService = adminService;
        this.memberService = memberService;
        this.memberRepo = memberRepo;
    }

    /**
     * 1) GET  /login
     *    - 회원/관리자 로그인 화면을 공통으로 보여줍니다.
     */
    @GetMapping("/login")
    public String loginPage(
        HttpSession session,
        @RequestParam(value = "redirectURL", required = false) String redirectURL
    ) {
        // redirectURL이 있으면 세션에 저장해둠
        if (redirectURL != null) {
            session.setAttribute("prevPage", redirectURL);
        }

        return "login/login";
    }

    /**
     * 2) POST /login
     *    - 폼의 hidden 필드(role)에 따라 회원/관리자를 구분하여 처리합니다.
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,     // "user" 또는 "admin"
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if ("admin".equals(role)) {
            // ───────────────────────────────────────────
            //   관리자 로그인 처리 (AdminDTO 사용)
            // ───────────────────────────────────────────
            boolean isAdmin = adminService.loginAdmin(username, password);
            if (!isAdmin) {
                model.addAttribute("error", "관리자 아이디 또는 비밀번호가 올바르지 않습니다.");
                return "login/login";
            }

            AdminDTO adminDto = adminService.findByCredentials(username, password)
                    .orElseThrow();

            // 세션에는 AdminDTO를 저장
            session.setAttribute("loggedInAdmin", adminDto);
            return "redirect:/admin/adminMain";
        }

        // ─────────────────────────────────────────────────────
        //   일반 회원 로그인 분기 (MemberDTO 사용)
        // ─────────────────────────────────────────────────────
        boolean ok = memberService.loginMember(username, password);
        if (!ok) {
            model.addAttribute("error", "회원 아이디 또는 비밀번호가 올바르지 않습니다.");
            return "login/login";
        }

        // 이제 MemberService.findMemberByCredentials()가 반환하는 Optional<MemberDTO>
        Optional<MemberDTO> optDto = memberService.findMemberByCredentials(username, password);
        MemberDTO memberDto = optDto.get();
        
        // 정지 상태 확인
        if (memberDto.getSuspendedUntil() != null && !memberDto.getSuspendedUntil().isBefore(LocalDate.now())) {
            long remaining = ChronoUnit.DAYS.between(LocalDate.now(), memberDto.getSuspendedUntil()) + 1;
            redirectAttributes.addFlashAttribute("error", "해당 계정은 " + remaining + "일간 정지 상태입니다.");
            return "redirect:/login";
        }

        // 세션에는 MemberDTO를 저장
        session.setAttribute("loggedInMember", memberDto);
        
        // 여기서 세션에 저장된 redirect 주소가 있는지 확인
        String redirectUrl = (String) session.getAttribute("prevPage");
        if (redirectUrl != null) {
            session.removeAttribute("prevPage"); // 다 썼으면 지워줌
            return "redirect:" + redirectUrl;    // 원래 주소로 이동
        }

        // 로그인 성공 후 일반 회원 메인 페이지로 리다이렉트
        return "redirect:/main";
    }

    /**
     * 3) GET /logout
     *    - 공통 로그아웃 처리: 세션 무효화하고 /login 으로 돌아감
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "login/signup";
    }

    /**
     * POST /signup
     *   회원가입 폼에서 전달받은 값을 처리하고, 성공하면 /login?signup=success 로 리다이렉트
     */
    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String username,         // memberId (이메일 사용)
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam String name,
            @RequestParam String nickname,
            @RequestParam String phone,
            Model model) {

        // 1) 비밀번호와 비밀번호 확인 일치 여부 체크
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "login/signup";
        }

        // 2) MemberDTO 생성 후 Service 호출
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(username);
        dto.setPwd(password);
        dto.setName(name);
        dto.setNickname(nickname);
        dto.setPhone(phone);
        // role, createAt, reportScore, suspendedUntil 등은 Service 내부에서 세팅됨

        try {
            Optional<MemberDTO> opt = memberService.registerMember(dto);
            if (opt.isEmpty()) {
                // memberId 중복으로 registerMember에서 Optional.empty() 리턴한 경우
                model.addAttribute("error", "이미 사용 중인 아이디입니다.");
                return "login/signup";
            }
        } catch (DataIntegrityViolationException ex) {
            // phone, nickname 중복 예외 처리
            String msg = ex.getMessage().toLowerCase();
            if (msg.contains("phone")) {
                model.addAttribute("error", "이미 사용 중인 전화번호입니다.");
            } else if (msg.contains("nickname")) {
                model.addAttribute("error", "이미 사용 중인 닉네임입니다.");
            } else {
                model.addAttribute("error", "회원가입 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
            return "login/signup";
        }

        // 4) 가입 성공 시 /login?signup=success 로 리다이렉트
        return "redirect:/login?signup=success";
    }

    // ──────────────────────────────────────────────────────────────────
    //  “닉네임/전화번호/아이디 중복확인” AJAX 엔드포인트
    // ──────────────────────────────────────────────────────────────────
    @PostMapping("/check-duplicate")
    @ResponseBody
    public String checkDuplicate(
            @RequestParam String type,
            @RequestParam String value) {

        boolean exists;
        if ("username".equals(type)) {
            exists = memberRepo.existsById(value);
        } else if ("nickname".equals(type)) {
            exists = memberRepo.existsByNickname(value);
        } else if ("phone".equals(type)) {
            exists = memberRepo.existsByPhone(value);
        } else {
            return "error";
        }

        return exists ? "duplicate" : "available";
    }
}
