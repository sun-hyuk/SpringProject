package com.dita.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private int reviewId;             // 꼭 필요! 신고, 좋아요 등에 사용
    private String nickname;          // 작성자 닉네임
    private int rating;               // 평점
    private String content;           // 리뷰 내용
    private LocalDateTime createdAt;  // 작성일자
    private List<String> imageUrls;   // 이미지 경로 리스트
    private int likeCount;            // 좋아요 수 (선택)
    private boolean liked;            // 로그인한 사용자가 좋아요 눌렀는지 여부 (선택)
    private String memberId;          // 작성자 ID
    private String profileImagePath;   // e.g. "user123.jpg"

    // ✅ [추가] 식당 이름 출력용 필드
    private String restaurantName;
    private Integer rstId;
    // ✅ [추가] 해당 식당의 리뷰 총 개수
    private int reviewCount;

    // ✅ [추가] 날짜 포맷용 getter (마이페이지에서 formattedCreatedAt 사용 시)
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
