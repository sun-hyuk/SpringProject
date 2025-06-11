package com.dita.service;

import com.dita.domain.Review;
import com.dita.domain.ReviewImage;
import com.dita.dto.ReviewDTO;
import com.dita.dto.ReviewWriteDTO;
import com.dita.persistence.ReviewImageRepository;
import com.dita.persistence.ReviewRepository;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {
	
	@Autowired
    private ServletContext servletContext;

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ReviewLikeService reviewLikeService;
    
    @Autowired
    private ReviewImageRepository reviewImageRepository;
    
    /**
     * 특정 식당(rstId)에 달린 리뷰 개수를 반환
     */
    public int countByRestaurant(Integer rstId) {
        // 반환 타입을 int 로 쓰고 싶으면 long → int 캐스팅
        return Math.toIntExact(reviewRepository.countByRestaurant_RstId(rstId));
    }
    
    public Page<ReviewDTO> getReviewsByRstIdSorted(int rstId, String sort, int page, int size, String loginUserId)
    {
        // 정렬 기준 설정
        Pageable pageable;
        switch (sort) {
            case "high":
                pageable = PageRequest.of(page, size, Sort.by("rating").descending());
                break;
            case "low":
                pageable = PageRequest.of(page, size, Sort.by("rating").ascending());
                break;
            default:
                pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        }

        // DB에서 리뷰 페이지 가져오기
        Page<Review> reviewPage = reviewRepository.findByRestaurant_RstId(rstId, pageable);

        // 리뷰를 ReviewDTO로 변환 (stream 이용)
        return reviewPage.map(r -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(r.getReviewId());
            dto.setNickname(r.getMember().getNickname());
            dto.setRating(r.getRating().intValue());
            dto.setContent(r.getContent());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setImageUrls(
                r.getReviewImages().stream()
                    .map(img -> img.getImage())
                    .collect(Collectors.toList())
            );
            dto.setLikeCount(reviewLikeService.countByReview(r.getReviewId()));
            dto.setLiked(loginUserId != null && reviewLikeService.isLikedByUser(loginUserId, r.getReviewId()));
            // ★ 여기에 프로필 이미지 경로 세팅
            dto.setProfileImagePath(r.getMember().getImage());
            dto.setReviewCount(countByRestaurant(rstId));  // 필요한 경우 DTO에 필드 추가!
            return dto;
        });
    }
    
    public Map<Integer, Integer> getRatingCounts(Integer rstId) {
        List<Object[]> rawData = reviewRepository.countReviewsByRating(rstId);
        Map<Integer, Integer> counts = new HashMap<>();

        // 1~5점까지 0으로 초기화
        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0);
        }

        for (Object[] row : rawData) {
            BigDecimal ratingValue = (BigDecimal) row[0];
            Long count = (Long) row[1];

            if (ratingValue != null) {
                int rating = ratingValue.intValue(); // 1~5점으로 변환
                counts.put(rating, count.intValue());
            }
        }

        return counts;
    }


    public double calculateAverageRating(Integer rstId) {
        List<Review> reviews = reviewRepository.findByRestaurant_RstId(rstId);
        if (reviews.isEmpty()) return 0.0;

        double sum = reviews.stream()
            .filter(r -> r.getRating() != null)
            .mapToDouble(r -> r.getRating().doubleValue())
            .sum();

        long count = reviews.stream().filter(r -> r.getRating() != null).count();
        return count == 0 ? 0.0 : Math.round(sum / count * 10.0) / 10.0;
    }
    
    
    
    /**
     * 관리자 -------------------------------------------------------
     */

    public List<ReviewDTO> getAllReviewsByRstId(int rstId) {
        List<Review> reviews = reviewRepository.findByRestaurant_RstId(rstId);

        return reviews.stream().map(r -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(r.getReviewId());
            dto.setNickname(r.getMember().getNickname());
            dto.setMemberId(r.getMember().getMemberId()); // 작성자 ID 설정
            dto.setRating(r.getRating().intValue());
            dto.setContent(r.getContent());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setImageUrls(
                r.getReviewImages().stream()
                    .map(img -> img.getImage())
                    .collect(Collectors.toList())
            );
            dto.setLikeCount(reviewLikeService.countByReview(r.getReviewId()));
            dto.setLiked(false); // 관리자 페이지에서는 로그인 여부 상관 없이 false 처리
            dto.setProfileImagePath(r.getMember().getImage());
            dto.setRstId(rstId);
            dto.setReviewCount(countByRestaurant(rstId));
            return dto;
        }).collect(Collectors.toList());
    }
    
    public void deleteReviewById(int reviewId) {
        reviewRepository.deleteById(reviewId);
    }
    
    // 리뷰 작성
    public void saveReview(ReviewWriteDTO dto, String memberId, List<MultipartFile> photos) {
        // 1. 리뷰 저장
        Review review = new Review();
        review.setContent(dto.getContent());
        review.setRating(BigDecimal.valueOf(dto.getRating()));
        review.setMemberId(memberId);
        review.setRstId(dto.getRstId());
        review.setMenu(dto.getMenuNames());
        review.setLikes(0);

        reviewRepository.save(review);

        // 2. 이미지가 여러 개 있는 경우 저장 처리
        if (photos != null && !photos.isEmpty()) {
        	String uploadDir = servletContext.getRealPath("/uploads/");
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    try {
                        String originalFileName = photo.getOriginalFilename();
                        String newFileName = UUID.randomUUID() + "_" + originalFileName;
                        File destFile = new File(uploadDir + newFileName);
                        photo.transferTo(destFile);

                        ReviewImage image = new ReviewImage();
                        image.setReview(review);
                        image.setImage("/uploads/" + newFileName);

                        // 위도/경도: 현재 null, 추후 JS에서 좌표 전송 시 확장 가능
                        image.setLatitude(null);
                        image.setLongitude(null);

                        reviewImageRepository.save(image);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 실패", e);
                    }
                }
            }
        }
    }
    
    // 마이페이지 리뷰 가져오기 : 회원 아이디로 리뷰 검색
    public List<ReviewDTO> getReviewsByMemberId(String memberId) {
        List<Review> reviews = reviewRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId);

        return reviews.stream().map(r -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(r.getReviewId());
            dto.setMemberId(memberId);
            dto.setNickname(r.getMember().getNickname());
            dto.setRating(r.getRating().intValue());
            dto.setContent(r.getContent());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setRestaurantName(r.getRestaurant().getName()); 
            dto.setRstId(r.getRestaurant().getRstId());
            dto.setImageUrls(
                r.getReviewImages().stream()
                    .map(ReviewImage::getImage)
                    .collect(Collectors.toList())
            );
            dto.setLikeCount(reviewLikeService.countByReview(r.getReviewId()));
            dto.setLiked(false); // 마이페이지에선 좋아요 여부 체크 생략 가능 (필요하면 session에서 memberId로 확인 가능)
            dto.setProfileImagePath(r.getMember().getImage());
            return dto;
        }).collect(Collectors.toList());
    }
}
