package com.dita.vo;

import com.dita.domain.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryVO {
    
    private Integer inquiryId;
    
    @NotNull(message = "문의 카테고리를 선택해주세요.")
    private String type;
    
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 50, message = "제목은 50자 이내로 입력해주세요.")
    private String title;
    
    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 500, message = "내용은 500자 이내로 입력해주세요.")
    private String content;
    
    private LocalDateTime createdAt;
    private String status;
    private String memberId;
    private String profileImagePath;

    // 날짜 포맷팅을 위한 메서드
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            return createdAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        }
        return "";
    }
    
    // Entity -> VO 변환
    public static InquiryVO fromEntity(Inquiry inquiry, String profileImagePath) {
        InquiryVO vo = new InquiryVO();
        vo.setInquiryId(inquiry.getInquiryId());
        vo.setType(inquiry.getType().name());
        vo.setTitle(inquiry.getTitle());
        vo.setContent(inquiry.getContent());
        vo.setCreatedAt(inquiry.getCreatedAt());
        vo.setStatus(inquiry.getStatus().name());
        vo.setMemberId(inquiry.getMemberId());
        vo.setProfileImagePath(profileImagePath);
        return vo;
    }
    
    // VO -> Entity 변환
    public Inquiry toEntity() {
        Inquiry inquiry = new Inquiry();
        inquiry.setInquiryId(this.inquiryId);
        inquiry.setType(Inquiry.InquiryType.valueOf(this.type));
        inquiry.setTitle(this.title);
        inquiry.setContent(this.content);
        inquiry.setCreatedAt(this.createdAt);
        if (this.status != null) {
            inquiry.setStatus(Inquiry.InquiryStatus.valueOf(this.status));
        }
        inquiry.setMemberId(this.memberId);
        return inquiry;
    }
    
    // 상태별 CSS 클래스 반환
    public String getStatusClass() {
        return "대기중".equals(status) ? "ongoing" : "completed";
    }
    
    // 타입별 한글 표시
    public String getTypeDisplayName() {
        switch (type) {
            case "계정문의": return "계정 문의";
            case "가계문의": return "가게 문의";
            case "기타": return "기타";
            default: return type;
        }
    }
}