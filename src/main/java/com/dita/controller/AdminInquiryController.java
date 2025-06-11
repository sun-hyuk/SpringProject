package com.dita.controller;

import com.dita.service.InquiryService;
import com.dita.vo.InquiryVO;
import com.dita.vo.InqCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminInquiryController {

    @Autowired
    private InquiryService inquiryService;

    /**
     * 현재 로그인한 관리자 ID 가져오기
     */
    private String getCurrentAdminId(HttpSession session) {
        try {
            Object adminInfo = session.getAttribute("loggedInAdmin");
            if (adminInfo != null) {
                if (adminInfo instanceof com.dita.dto.AdminDTO) {
                    com.dita.dto.AdminDTO adminDTO = (com.dita.dto.AdminDTO) adminInfo;
                    return adminDTO.getAdminId();
                }
                else if (adminInfo instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> adminMap = (java.util.Map<String, Object>) adminInfo;
                    Object adminId = adminMap.get("adminId");
                    return adminId != null ? adminId.toString() : null;
                }
                else {
                    return adminInfo.toString();
                }
            }
            
            System.out.println("Warning: 관리자 세션 정보가 없습니다.");
            return null;
        } catch (Exception e) {
            System.out.println("관리자 세션 정보 가져오기 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 관리자 문의 관리 페이지
     */
    @GetMapping("/adminInquiry")
    public String adminInquiryPage(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        try {
            List<InquiryVO> filteredInquiries = inquiryService.getInquiriesForAdmin(status, type, keyword);
            
            long totalCount = inquiryService.getTotalInquiryCount();
            long pendingCount = inquiryService.getInquiryCountByStatus("대기중");
            long completedCount = inquiryService.getInquiryCountByStatus("완료");
            
            model.addAttribute("inquiryList", filteredInquiries);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("completedCount", completedCount);
            
            model.addAttribute("status", status);
            model.addAttribute("type", type);
            model.addAttribute("keyword", keyword);
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "문의 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("inquiryList", List.of());
            model.addAttribute("totalCount", 0);
            model.addAttribute("pendingCount", 0);
            model.addAttribute("completedCount", 0);
        }
        
        return "admin/adminInquiry";
    }

    /**
     * 관리자 문의 상세 페이지
     */
    @GetMapping("/adminInquiryDetail/{inquiryId}")
    public String adminInquiryDetailWithId(@PathVariable Integer inquiryId, Model model) {
        try {
            InquiryVO inquiry = inquiryService.getInquiryDetailForAdmin(inquiryId);
            model.addAttribute("inquiry", inquiry);
            
            List<InqCommentVO> comments = inquiryService.getInquiryComments(inquiryId);
            model.addAttribute("comments", comments);
            model.addAttribute("hasComments", !comments.isEmpty());
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "문의 상세 정보를 불러오는 중 오류가 발생했습니다.");
        }
        
        return "admin/adminInquiryDetail";
    }

    /**
     * 디자인용 상세 페이지
     */
    @GetMapping("/adminInquiryDetail")
    public String adminInquiryDetailPage(Model model) {
        Map<String, Object> inquiry = new HashMap<>();
        inquiry.put("inquiryId", 1);
        inquiry.put("type", "계정문의");
        inquiry.put("title", "로그인 오류 문의");
        inquiry.put("content", "로그인 시도 시 오류가 발생합니다. 확인 부탁드립니다.");
        inquiry.put("memberId", "testUser");
        inquiry.put("formattedCreatedAt", "2023-06-01");
        inquiry.put("status", "대기중");
        
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("comments", List.of());
        model.addAttribute("hasComments", false);
        
        return "admin/adminInquiryDetail";
    }

    /**
     * 문의 답변 등록 (AJAX)
     */
    @PostMapping("/api/admin/inquiry/{inquiryId}/reply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitInquiryReply(
            @PathVariable Integer inquiryId, 
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("=== 답변 등록 시작 ===");
        System.out.println("inquiryId: " + inquiryId);
        System.out.println("request: " + request);
        
        try {
            // 1. 요청 데이터 검증
            String comments = request.get("reply");
            System.out.println("답변 내용: " + comments);
            
            if (comments == null || comments.trim().isEmpty()) {
                System.out.println("오류: 답변 내용이 비어있음");
                response.put("success", false);
                response.put("message", "답변 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2. 관리자 로그인 확인
            String adminId = getCurrentAdminId(session);
            System.out.println("관리자 ID: " + adminId);
            
            if (adminId == null || adminId.trim().isEmpty()) {
                System.out.println("오류: 관리자가 로그인하지 않음");
                response.put("success", false);
                response.put("message", "관리자 로그인이 필요합니다. 로그인 후 다시 시도해주세요.");
                return ResponseEntity.status(401).body(response);
            }
            
            // 3. 문의 존재 확인
            System.out.println("문의 존재 확인 중...");
            InquiryVO inquiry = inquiryService.getInquiryDetailForAdmin(inquiryId);
            if (inquiry == null) {
                System.out.println("오류: 문의를 찾을 수 없음");
                response.put("success", false);
                response.put("message", "해당 문의를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            System.out.println("문의 정보: " + inquiry.getTitle());
            
            // 4. 답변 등록
            System.out.println("답변 등록 시도 중...");
            InqCommentVO savedComment = inquiryService.createInquiryReply(inquiryId, comments, adminId);
            System.out.println("답변 등록 성공: " + savedComment.getInqCommentId());
            
            response.put("success", true);
            response.put("message", "답변이 등록되고 문의가 완료 처리되었습니다.");
            response.put("comment", savedComment);
            
            System.out.println("=== 답변 등록 완료 ===");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "답변 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 문의 상태 업데이트 (AJAX)
     */
    @PutMapping("/api/admin/inquiry/{inquiryId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateInquiryStatus(
            @PathVariable Integer inquiryId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String status = request.get("status");
            
            if (status == null || status.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "상태 값이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            inquiryService.updateInquiryStatus(inquiryId, status);
            
            response.put("success", true);
            response.put("message", "상태가 성공적으로 업데이트되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "상태 업데이트 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 특정 문의의 답변 목록 조회 (AJAX)
     */
    @GetMapping("/api/admin/inquiry/{inquiryId}/comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInquiryComments(@PathVariable Integer inquiryId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<InqCommentVO> comments = inquiryService.getInquiryComments(inquiryId);
            
            response.put("success", true);
            response.put("comments", comments);
            response.put("count", comments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "답변 목록을 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 문의 삭제 (관리자용) - AJAX
     */
    @DeleteMapping("/api/admin/inquiry/{inquiryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteInquiry(@PathVariable Integer inquiryId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            inquiryService.deleteInquiry(inquiryId, null);
            
            response.put("success", true);
            response.put("message", "문의가 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "문의 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 통계 정보 조회 (AJAX)
     */
    @GetMapping("/api/admin/inquiry/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInquiryStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", inquiryService.getTotalInquiryCount());
            stats.put("pending", inquiryService.getInquiryCountByStatus("대기중"));
            stats.put("completed", inquiryService.getInquiryCountByStatus("완료"));
            
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "통계 정보를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 문의 완료 처리 (답변 없이) - AJAX
     */
    @PostMapping("/api/admin/inquiry/{inquiryId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeInquiry(@PathVariable Integer inquiryId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            inquiryService.completeInquiry(inquiryId);
            
            response.put("success", true);
            response.put("message", "문의가 완료 처리되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "문의 완료 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 답변 수정 (AJAX)
     */
    @PutMapping("/api/admin/inquiry/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateInquiryComment(
            @PathVariable Integer commentId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String newComments = request.get("comments");
            if (newComments == null || newComments.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "답변 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String adminId = getCurrentAdminId(session);
            InqCommentVO updatedComment = inquiryService.updateInquiryComment(commentId, newComments, adminId);
            
            response.put("success", true);
            response.put("message", "답변이 수정되었습니다.");
            response.put("comment", updatedComment);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "답변 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 답변 삭제 (AJAX)
     */
    @DeleteMapping("/api/admin/inquiry/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteInquiryComment(
            @PathVariable Integer commentId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String adminId = getCurrentAdminId(session);
            inquiryService.deleteInquiryComment(commentId, adminId);
            
            response.put("success", true);
            response.put("message", "답변이 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "답변 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }
}