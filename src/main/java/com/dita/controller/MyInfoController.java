package com.dita.controller;

import com.dita.dto.MemberDTO;
import com.dita.service.MemberService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/myInfo")
public class MyInfoController {

    private static final Logger logger = LoggerFactory.getLogger(MyInfoController.class);

    private final MemberService memberService;
    private final ServletContext servletContext; // 톰캣 웹앱 루트 경로를 얻기 위해 주입

    public MyInfoController(MemberService memberService,
                            ServletContext servletContext) {
        this.memberService = memberService;
        this.servletContext = servletContext;
    }

    /** 내 정보 수정 메인 (/myInfo) */
    @GetMapping
    public String myInfo(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            logger.info(">> [MyInfoController] /myInfo – 비로그인 상태, /login 리다이렉트");
            return "redirect:/login";
        }
        logger.info(">> [MyInfoController] /myInfo – 로그인된 회원: {}", member.getMemberId());

        model.addAttribute("member", member);
        return "myInfo/myInfo";
    }

    /** 프로필 이미지 업로드 처리 (POST /myInfo/uploadImage) */
    @PostMapping("/uploadImage")
    public String uploadImage(
            @RequestParam("profileImage") MultipartFile file,
            HttpSession session,
            Model model) throws IOException {

        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 1) 파일 유효성 검증
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "업로드할 이미지를 선택해주세요.");
            model.addAttribute("member", member);
            return "myInfo/myInfo";
        }

        // 2) 톰캣 웹앱 루트 아래 uploads/profiles 디렉터리 실제 경로 얻기
        // 예: {톰캣 홈}/webapps/ROOT/uploads/profiles
        String realPath = servletContext.getRealPath("/uploads/profiles");
        File dir = new File(realPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3) 원본 파일명에서 확장자만 추출 (Commons IO 없이 순수 Java)
        String originalName = file.getOriginalFilename();  // 예: "avatar.png"
        String ext = "";
        if (originalName != null) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                ext = originalName.substring(dotIndex + 1); // "png"
            }
        }

        // 4) UUID + 확장자로 새로운 파일명 생성
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String newFilename = ext.isEmpty() ? uuid : uuid + "." + ext;

        // 5) 파일을 물리적으로 저장
        File dest = new File(dir, newFilename);
        file.transferTo(dest);

        // 6) DB에 저장할 이미지 경로 설정 (브라우저에서 /uploads/profiles/ 파일 호출 가능)
        String relativePath = "/uploads/profiles/" + newFilename;
        memberService.updateProfileImage(member.getMemberId(), relativePath);

        // 7) 세션에 들어있는 DTO도 갱신
        MemberDTO updated = memberService.findByMemberId(member.getMemberId());
        session.setAttribute("loggedInMember", updated);

        // 8) 마이페이지로 리다이렉트
        return "redirect:/myInfo";
    }

    /** 닉네임 변경 (/myInfo/nickname) */
    @GetMapping("/nickname")
    public String changeNickname(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", member);
        return "myInfo/nickname";
    }

    /** 닉네임 변경 처리 (POST /myInfo/nickname) */
    @PostMapping("/nickname")
    public String updateNickname(
            @RequestParam String nickname,
            HttpSession session) {

        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        memberService.updateNickname(member.getMemberId(), nickname);

        MemberDTO updated = memberService.findByMemberId(member.getMemberId());
        session.setAttribute("loggedInMember", updated);

        return "redirect:/myInfo";
    }

    /** 비밀번호 변경 (/myInfo/password) */
    @GetMapping("/password")
    public String changePassword(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", member);
        return "myInfo/password";
    }

    /** 비밀번호 변경 처리 (POST /myInfo/password) */
    @PostMapping("/password")
    public String updatePassword(
            @RequestParam String currentPwd,
            @RequestParam String newPwd,
            @RequestParam String confirmPwd,
            HttpSession session,
            Model model) {

        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 1) 새 비밀번호 & 확인 일치 여부 검사
        if (!newPwd.equals(confirmPwd)) {
            model.addAttribute("error", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            model.addAttribute("member", member);
            return "myInfo/password";
        }

        // 2) 현재 비밀번호 유효성 검사
        boolean matches = memberService.checkPassword(member.getMemberId(), currentPwd);
        if (!matches) {
            model.addAttribute("error", "현재 비밀번호가 올바르지 않습니다.");
            model.addAttribute("member", member);
            return "myInfo/password";
        }

        // 3) 비밀번호 업데이트
        memberService.updatePassword(member.getMemberId(), newPwd);

        // 4) 세션에 들어있는 DTO도 갱신
        MemberDTO updated = memberService.findByMemberId(member.getMemberId());
        session.setAttribute("loggedInMember", updated);

        // 5) 완료 후 마이페이지로 리다이렉트
        return "redirect:/myInfo";
    }

    /** 로그아웃 (/myInfo/logout) */
    @GetMapping("/logout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /** 회원 탈퇴 (/myInfo/withdrawal) */
    @GetMapping("/withdrawal")
    public String changeWithdrawal(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", member);
        return "myInfo/withdrawal";
    }
    
    /** 회원 탈퇴 처리 (POST /myInfo/withdrawal) */
    @PostMapping("/withdrawal")
    public String processWithdrawal(
            HttpSession session,
            @RequestParam(value = "agree", required = false) String agree,
            Model model) {

        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 체크박스 검증(보조): agree 파라미터가 존재해야 동의로 간주
        if (agree == null) {
            model.addAttribute("error", "유의사항에 동의하셔야 탈퇴가 가능합니다.");
            model.addAttribute("member", member);
            return "myInfo/withdrawal";
        }

        // 1) DB에서 실제 회원 데이터 삭제
        memberService.deleteMemberById(member.getMemberId());

        // 2) 세션 무효화
        session.invalidate();

        // 3) 탈퇴 완료 후 메인 페이지 또는 로그인 페이지로 이동
        return "redirect:/";  // 혹은 "redirect:/login"
    }
}
