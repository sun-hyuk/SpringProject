package com.dita.service;

import com.dita.persistence.InquiryRepository;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.InqCommentRepository;
import com.dita.domain.Inquiry;
import com.dita.domain.Member;
import com.dita.domain.Alerts;
import com.dita.domain.InqComment;
import com.dita.vo.InquiryVO;
import com.dita.vo.InqCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class InquiryService {
    
    @Autowired
    private InquiryRepository inquiryRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private InqCommentRepository inqCommentDAO;
    
    @Autowired
    private AlertsService alertsService;
    
    /**
     * 문의 등록
     */
    public InquiryVO createInquiry(InquiryVO inquiryVO) {
        Inquiry inquiry = inquiryVO.toEntity();
        inquiry.setStatus(Inquiry.InquiryStatus.대기중);
        Inquiry saved = inquiryRepository.save(inquiry);
        // 프로필 경로 조회
        Member m = memberRepository.findById(saved.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Member not found: " + saved.getMemberId()));
        String img = m.getImage();
        return InquiryVO.fromEntity(saved, img);
    }

    /**
     * 전체 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InquiryVO> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();
        return inquiries.stream()
            .map(inq -> {
                Member m = memberRepository.findById(inq.getMemberId()).orElse(null);
                String img = (m != null ? m.getImage() : "/images/default-profile.png");
                return InquiryVO.fromEntity(inq, img);
            })
            .collect(Collectors.toList());
    }

    /**
     * 회원별 문의 목록 조회 (전체)
     */
    @Transactional(readOnly = true)
    public List<InquiryVO> getInquiriesByMemberId(String memberId) {
        if (memberId == null) return List.of();
        List<Inquiry> inquiries = inquiryRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        return inquiries.stream()
            .map(inq -> {
                Member m = memberRepository.findById(inq.getMemberId())
                        .orElseThrow(() -> new NoSuchElementException("Member not found: " + inq.getMemberId()));
                String img = m.getImage();
                return InquiryVO.fromEntity(inq, img);
            })
            .collect(Collectors.toList());
    }

    /**
     * 회원별 문의 목록 조회 (상태별)
     */
    @Transactional(readOnly = true)
    public List<InquiryVO> getInquiriesByMemberIdAndStatus(String memberId, String status) {
        List<Inquiry> inquiries;
        if (memberId == null) {
            inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();
        } else {
            Inquiry.InquiryStatus st = Inquiry.InquiryStatus.valueOf(status);
            inquiries = inquiryRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, st);
        }
        return inquiries.stream()
            .map(inq -> {
                Member m = memberRepository.findById(inq.getMemberId()).orElse(null);
                String img = (m != null ? m.getImage() : "/images/default-profile.png");
                return InquiryVO.fromEntity(inq, img);
            })
            .collect(Collectors.toList());
    }

    /**
     * 문의 상세 조회
     */
    @Transactional(readOnly = true)
    public InquiryVO getInquiryDetail(Integer inquiryId, String memberId) {
        Inquiry inq;
        if (memberId == null) {
            inq = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));
        } else {
            inq = inquiryRepository.findByInquiryIdAndMemberId(inquiryId, memberId);
            if (inq == null) throw new IllegalArgumentException("Inquiry not found: " + inquiryId);
        }
        Member m = memberRepository.findById(inq.getMemberId()).orElse(null);
        String img = (m != null ? m.getImage() : "/images/default-profile.png");
        return InquiryVO.fromEntity(inq, img);
    }

    /**
     * 문의 삭제
     */
    public void deleteInquiry(Integer inquiryId, String memberId) {
        Inquiry inq;
        if (memberId == null) {
            inq = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));
        } else {
            inq = inquiryRepository.findByInquiryIdAndMemberId(inquiryId, memberId);
            if (inq == null) throw new IllegalArgumentException("Inquiry not found: " + inquiryId);
        }
        inquiryRepository.delete(inq);
    }

    /**
     * 문의 개수 조회
     */
    @Transactional(readOnly = true)
    public long getInquiryCountByMemberId(String memberId) {
        if (memberId == null) return inquiryRepository.count();
        return inquiryRepository.countByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public long getInquiryCountByMemberIdAndStatus(String memberId, String status) {
        Inquiry.InquiryStatus st = Inquiry.InquiryStatus.valueOf(status);
        if (memberId == null) return inquiryRepository.countByStatus(st);
        return inquiryRepository.countByMemberIdAndStatus(memberId, st);
    }
    
    
    /**
     * 문의 상태 업데이트
     */
    public void updateInquiryStatus(Integer inquiryId, String status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));
        
        inquiry.setStatus(Inquiry.InquiryStatus.valueOf(status));
        inquiryRepository.save(inquiry);
    }
    
    // ===========================================
    // 관리자용 메서드들
    // ===========================================
    
    /**
     * 전체 문의 개수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalInquiryCount() {
        return inquiryRepository.count();
    }
    
    /**
     * 상태별 문의 개수 조회
     */
    @Transactional(readOnly = true)
    public long getInquiryCountByStatus(String status) {
        Inquiry.InquiryStatus inquiryStatus = Inquiry.InquiryStatus.valueOf(status);
        return inquiryRepository.countByStatus(inquiryStatus);
    }
    
    /**
     * 관리자용 문의 목록 조회 (필터링 포함)
     */
    @Transactional(readOnly = true)
    public List<InquiryVO> getInquiriesForAdmin(String status, String type, String keyword) {
        List<InquiryVO> allInquiries = getAllInquiries();
        
        return allInquiries.stream()
                .filter(inquiry -> {
                    if (status != null && !status.isEmpty() && !status.equals("all")) {
                        if (!inquiry.getStatus().equals(status)) {
                            return false;
                        }
                    }
                    
                    if (type != null && !type.isEmpty() && !type.equals("all")) {
                        if (!inquiry.getType().equals(type)) {
                            return false;
                        }
                    }
                    
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        if (!inquiry.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 관리자용 문의 상세 조회
     */
    @Transactional(readOnly = true)
    public InquiryVO getInquiryDetailForAdmin(Integer inquiryId) {
        return getInquiryDetail(inquiryId, null);
    }
    
    /**
     * 문의 완료 처리
     */
    public void completeInquiry(Integer inquiryId) {
        updateInquiryStatus(inquiryId, "완료");
    }
    
    /**
     * 대기중인 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InquiryVO> getPendingInquiries() {
        return getAllInquiries().stream()
                .filter(inquiry -> "대기중".equals(inquiry.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * 완료된 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InquiryVO> getCompletedInquiries() {
        return getAllInquiries().stream()
                .filter(inquiry -> "완료".equals(inquiry.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 회원의 문의 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getMemberInquiryStats(String memberId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", inquiryRepository.countByMemberId(memberId));
        stats.put("pending", inquiryRepository.countByMemberIdAndStatus(memberId, Inquiry.InquiryStatus.대기중));
        stats.put("completed", inquiryRepository.countByMemberIdAndStatus(memberId, Inquiry.InquiryStatus.완료));
        return stats;
    }
    
    // ===========================================
    // 답변(InqComment) 관련 메서드들
    // ===========================================
    
    /**
     * 문의에 답변 등록
     */
    public InqCommentVO createInquiryReply(Integer inquiryId, String comments, String adminId) {
        System.out.println("=== 답변 등록 시작 ===");
        System.out.println("inquiryId: " + inquiryId);
        System.out.println("comments: " + comments);
        System.out.println("adminId: " + adminId);
        
        // 1. 문의 존재 확인
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));
        
        try {
            // 2. 답변 엔티티 생성
            InqComment inqComment = new InqComment();
            inqComment.setInquiryId(inquiryId);
            inqComment.setComments(comments);
            
            // 3. adminId 설정
            if (adminId != null && !adminId.trim().isEmpty()) {
                inqComment.setAdminId(adminId.trim());
                System.out.println("adminId 설정: " + adminId);
            } else {
                inqComment.setAdminId(null);
                System.out.println("adminId NULL로 설정");
            }
            
            // 4. 답변 저장
            InqComment savedComment = inqCommentDAO.save(inqComment);
            System.out.println("답변 저장 완료: ID = " + savedComment.getInqCommentId());
            
            // 5. 문의 상태 완료로 변경
            inquiry.setStatus(Inquiry.InquiryStatus.완료);
            inquiryRepository.save(inquiry);
            System.out.println("문의 상태 변경 완료");
            
            // 6. 알림 생성
            if (inquiry.getMemberId() != null) {
                alertsService.createAlert(
                    inquiry.getMemberId(), // 문자열 memberId
                    Alerts.AlertType.문의,
                    inquiryId
                );
                System.out.println("알림 생성 완료");
            }
            
            return InqCommentVO.fromEntity(savedComment);
            
        } catch (Exception e) {
            System.out.println("답변 등록 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("답변 등록 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    public InquiryVO getInquiryById(int inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new NoSuchElementException("해당 문의가 존재하지 않습니다."));

        // Member 조회해서 image 경로 꺼내기
        Member m = memberRepository.findById(inquiry.getMemberId())
            .orElse(null);
        String img = (m != null ? m.getImage() : "/images/default-profile.png");

        // 두 번째 인자로 프로필 이미지 경로 전달
        return InquiryVO.fromEntity(inquiry, img);
    }

    /**
     * 특정 문의의 모든 답변 조회
     */
    @Transactional(readOnly = true)
    public List<InqCommentVO> getInquiryComments(Integer inquiryId) {
        try {
            if (inqCommentDAO == null) {
                return new ArrayList<>();
            }
            
            List<InqComment> comments = inqCommentDAO.findByInquiryIdOrderByCreateAtAsc(inquiryId);
            return comments.stream()
                    .map(InqCommentVO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Warning: Could not load comments: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 특정 문의의 가장 최근 답변 조회
     */
    @Transactional(readOnly = true)
    public InqCommentVO getLatestInquiryComment(Integer inquiryId) {
        try {
            if (inqCommentDAO == null) {
                return null;
            }
            
            return inqCommentDAO.findTopByInquiryIdOrderByCreateAtDesc(inquiryId)
                    .map(InqCommentVO::fromEntity)
                    .orElse(null);
        } catch (Exception e) {
            System.out.println("Warning: Could not load latest comment: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 문의에 답변이 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasInquiryComments(Integer inquiryId) {
        try {
            if (inqCommentDAO == null) {
                return false;
            }
            return inqCommentDAO.countByInquiryId(inquiryId) > 0;
        } catch (Exception e) {
            System.out.println("Warning: Could not check comments: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 특정 관리자가 작성한 답변 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InqCommentVO> getCommentsByAdmin(String adminId) {
        try {
            if (inqCommentDAO == null) {
                return new ArrayList<>();
            }
            
            List<InqComment> comments = inqCommentDAO.findByAdminIdOrderByCreateAtDesc(adminId);
            return comments.stream()
                    .map(InqCommentVO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Warning: Could not load comments by admin: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 답변 삭제
     */
    public void deleteInquiryComment(Integer commentId, String adminId) {
        try {
            if (inqCommentDAO == null) {
                throw new RuntimeException("답변 기능이 활성화되지 않았습니다.");
            }
            
            InqComment comment = inqCommentDAO.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
            
            inqCommentDAO.delete(comment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("답변 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 답변 수정
     */
    public InqCommentVO updateInquiryComment(Integer commentId, String newComments, String adminId) {
        try {
            if (inqCommentDAO == null) {
                throw new RuntimeException("답변 기능이 활성화되지 않았습니다.");
            }
            
            InqComment comment = inqCommentDAO.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
            
            comment.setComments(newComments);
            InqComment updatedComment = inqCommentDAO.save(comment);
            
            return InqCommentVO.fromEntity(updatedComment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("답변 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}