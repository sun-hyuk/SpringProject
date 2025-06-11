package com.dita.controller;

import com.dita.service.InquiryService;
import com.dita.vo.InquiryVO;
import com.dita.vo.InqCommentVO;
import com.dita.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InquiryController {

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

    /**
     * 문의하기 폼 페이지
     */
    @GetMapping("/inquiry")
    public String showInquiryForm(Model model, HttpSession session) {
        String memberId = getCurrentMemberId(session);
        String memberNickname = getCurrentMemberNickname(session);
        
        if (memberId != null) {
            List<InquiryVO> inquiries = inquiryService.getInquiriesByMemberId(memberId);
            long totalCount = inquiryService.getInquiryCountByMemberId(memberId);
            long ongoingCount = inquiryService.getInquiryCountByMemberIdAndStatus(memberId, "대기중");
            long completedCount = inquiryService.getInquiryCountByMemberIdAndStatus(memberId, "완료");
            
            model.addAttribute("inquiries", inquiries);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("ongoingCount", ongoingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("currentMemberId", memberId);
            model.addAttribute("currentMemberNickname", memberNickname);
        } else {
            model.addAttribute("inquiries", List.of());
            model.addAttribute("totalCount", 0);
            model.addAttribute("ongoingCount", 0);
            model.addAttribute("completedCount", 0);
            model.addAttribute("loginRequired", true);
        }
        
        model.addAttribute("memberId", memberId);
        model.addAttribute("memberNickname", memberNickname);
        return "inquiry/inquiry";
    }

    /**
     * 문의 상세 페이지
     */
    @GetMapping("/inquiryDetail/{inquiryId}")
    public String showInquiryDetail(@PathVariable Integer inquiryId, Model model, HttpSession session) {
        String memberId = getCurrentMemberId(session);
        String memberNickname = getCurrentMemberNickname(session);
        
        if (memberId == null) {
            return "redirect:/login";
        }
        
        try {
            InquiryVO inquiry = inquiryService.getInquiryDetail(inquiryId, memberId);
            model.addAttribute("inquiry", inquiry);
            model.addAttribute("currentMemberId", memberId);
            model.addAttribute("currentMemberNickname", memberNickname);
            
            // 답변 목록 추가
            try {
                List<InqCommentVO> comments = inquiryService.getInquiryComments(inquiryId);
                model.addAttribute("comments", comments);
            } catch (Exception e) {
                model.addAttribute("comments", List.of());
                System.out.println("Warning: Could not load comments for inquiry " + inquiryId + ": " + e.getMessage());
            }
            
            return "inquiry/inquiryDetail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentMemberId", memberId);
            model.addAttribute("currentMemberNickname", memberNickname);
            model.addAttribute("comments", List.of());
            return "inquiry/inquiryDetail";
        }
    }
    
    /**
     * 디자인용 상세페이지
     */
    @GetMapping("/inquiryDetail")
    public String showInquiryDetail() {
        return "inquiry/inquiryDetail";
    }

    /**
     * 문의 등록 처리 (AJAX)
     */
    @PostMapping("/inquiry/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createInquiry(@Valid @RequestBody InquiryVO inquiryVO, 
                                                            BindingResult bindingResult,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String memberId = getCurrentMemberId(session);
        if (memberId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "입력값을 확인해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            inquiryVO.setMemberId(memberId);
            InquiryVO savedInquiry = inquiryService.createInquiry(inquiryVO);
            
            response.put("success", true);
            response.put("message", "문의가 성공적으로 등록되었습니다.");
            response.put("inquiry", savedInquiry);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "문의 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 문의 목록 조회 (AJAX)
     */
    @GetMapping("/inquiry/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInquiryList(@RequestParam(required = false) String status,
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String memberId = getCurrentMemberId(session);
        if (memberId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            List<InquiryVO> inquiries;
            
            if (status != null && !status.equals("all")) {
                inquiries = inquiryService.getInquiriesByMemberIdAndStatus(memberId, status);
            } else {
                inquiries = inquiryService.getInquiriesByMemberId(memberId);
            }
            
            response.put("success", true);
            response.put("inquiries", inquiries);
            response.put("count", inquiries.size());
            response.put("memberId", memberId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "문의 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 문의 삭제 (AJAX)
     */
    @DeleteMapping("/inquiry/{inquiryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteInquiry(@PathVariable Integer inquiryId,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String memberId = getCurrentMemberId(session);
        if (memberId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            inquiryService.deleteInquiry(inquiryId, memberId);
            
            response.put("success", true);
            response.put("message", "문의가 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "문의 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 문의 상세 조회 (AJAX)
     */
    @GetMapping("/inquiry/{inquiryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInquiryDetail(@PathVariable Integer inquiryId,
                                                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String memberId = getCurrentMemberId(session);
        if (memberId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            InquiryVO inquiry = inquiryService.getInquiryDetail(inquiryId, memberId);
            
            response.put("success", true);
            response.put("inquiry", inquiry);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "문의 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 세션 확인용 API (디버깅용)
     */
    @GetMapping("/inquiry/check-session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String memberId = getCurrentMemberId(session);
        
        Map<String, Object> sessionAttributes = new HashMap<>();
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            sessionAttributes.put(attributeName, attributeValue);
        }
        
        response.put("success", true);
        response.put("memberId", memberId);
        response.put("sessionId", session.getId());
        response.put("allAttributes", sessionAttributes);
        response.put("isLoggedIn", memberId != null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 임시 세션 설정 (디버깅용)
     */
    @PostMapping("/inquiry/set-session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> setSession(@RequestParam String key, 
                                                          @RequestParam String value,
                                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        session.setAttribute(key, value);
        
        response.put("success", true);
        response.put("message", "세션 설정 완료: " + key + " = " + value);
        
        return ResponseEntity.ok(response);
    }
}