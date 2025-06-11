package com.dita.controller;

import com.dita.service.InquiryService;
import com.dita.vo.InquiryVO;
import com.dita.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class MyInquiryController {

    @Autowired
    private InquiryService inquiryService;

    /**
     * 현재 로그인한 사용자 정보 가져오기
     */
    private String getCurrentMemberId(HttpSession session) {
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        if (loggedInMember != null) {
            return loggedInMember.getMemberId();
        }
        return null;
    }

    /**
     * 현재 로그인한 사용자 닉네임 가져오기
     */
    private String getCurrentMemberNickname(HttpSession session) {
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        if (loggedInMember != null) {
            return loggedInMember.getNickname();
        }
        return null;
    }

    @GetMapping("/myInquiry")
    public String myInquiryPage(Model model, HttpSession session) {
        try {
            String memberId = getCurrentMemberId(session);
            String memberNickname = getCurrentMemberNickname(session);
            
            if (memberId != null) {
                // 1) VO 리스트 가져오기
                List<InquiryVO> inquiries = inquiryService.getInquiriesByMemberId(memberId);

                // 2) 세션에서 프로필 이미지 경로 가져오기
                MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
                // 예) MemberDTO에 getImage() 또는 getProfileImagePath() 가 있다면
                String profileImagePath = (loggedInMember != null)
                    ? loggedInMember.getImage()   // 혹은 getProfileImagePath()
                    : "/images/default-profile.png";

                // 3) VO마다 profileImagePath 세팅
                for (InquiryVO vo : inquiries) {
                    vo.setProfileImagePath(profileImagePath);
                }

                long totalCount = inquiryService.getInquiryCountByMemberId(memberId);
                long ongoingCount = inquiryService.getInquiryCountByMemberIdAndStatus(memberId, "대기중");
                long completedCount = inquiryService.getInquiryCountByMemberIdAndStatus(memberId, "완료");

                model.addAttribute("inquiries", inquiries);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("ongoingCount", ongoingCount);
                model.addAttribute("completedCount", completedCount);
                model.addAttribute("currentMemberId", memberId);
                model.addAttribute("currentMemberNickname", memberNickname);
                model.addAttribute("loginRequired", false);
            } else {
                model.addAttribute("inquiries", List.of());
                model.addAttribute("totalCount", 0);
                model.addAttribute("ongoingCount", 0);
                model.addAttribute("completedCount", 0);
                model.addAttribute("loginRequired", true);
            }

            model.addAttribute("memberId", memberId);
            model.addAttribute("memberNickname", memberNickname);
            return "myInquiry"; 
        } catch (Exception e) {
            model.addAttribute("inquiries", List.of());
            model.addAttribute("totalCount", 0);
            model.addAttribute("ongoingCount", 0);
            model.addAttribute("completedCount", 0);
            model.addAttribute("loginRequired", true);
            model.addAttribute("error", "페이지 로딩 중 오류가 발생했습니다.");
            return "myInquiry";
        }
    }
}