package com.dita.vo;

import com.dita.domain.InqComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InqCommentVO {
    
    private Integer inqCommentId;
    
    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(max = 500, message = "답변은 500자 이내로 입력해주세요.")
    private String comments;
    
    private LocalDateTime createAt;
    private String adminId;
    private Integer inquiryId;
    
    // 날짜 포맷팅을 위한 메서드
    public String getFormattedCreateAt() {
        if (createAt != null) {
            return createAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        }
        return "";
    }
    
    // 완전한 날짜 포맷
    public String getFullFormattedCreateAt() {
        if (createAt != null) {
            return createAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return "";
    }
    
    // 관리자 표시명 (항상 "관리자"로 표시)
    public String getAdminDisplayName() {
        return "관리자";
    }
    
    // Entity -> VO 변환
    public static InqCommentVO fromEntity(InqComment inqComment) {
        if (inqComment == null) {
            return null;
        }
        
        InqCommentVO vo = new InqCommentVO();
        vo.setInqCommentId(inqComment.getInqCommentId());
        vo.setComments(inqComment.getComments());
        vo.setCreateAt(inqComment.getCreateAt());
        vo.setAdminId(inqComment.getAdminId());
        vo.setInquiryId(inqComment.getInquiryId());
        return vo;
    }
    
    // VO -> Entity 변환
    public InqComment toEntity() {
        InqComment inqComment = new InqComment();
        inqComment.setInqCommentId(this.inqCommentId);
        inqComment.setComments(this.comments);
        inqComment.setCreateAt(this.createAt);
        inqComment.setAdminId(this.adminId);
        inqComment.setInquiryId(this.inquiryId);
        return inqComment;
    }
}