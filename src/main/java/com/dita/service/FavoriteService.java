// src/main/java/com/dita/service/FavoriteService.java
package com.dita.service;

import com.dita.domain.Member;
import com.dita.domain.RstJjim;
import com.dita.domain.Restaurant;
import com.dita.dto.RstJjimDTO;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.RstJjimRepository;
import com.dita.persistence.RestaurantRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  // ← 추가
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;
//✅ 올바른 import
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FavoriteService {
    private final RstJjimRepository rstJjimRepo;
    private final MemberRepository memberRepo;
    private final RestaurantRepository restaurantRepo;

    public FavoriteService(RstJjimRepository rstJjimRepo,
                           MemberRepository memberRepo,
                           RestaurantRepository restaurantRepo) {
        this.rstJjimRepo = rstJjimRepo;
        this.memberRepo = memberRepo;
        this.restaurantRepo = restaurantRepo;
    }
    
    // 현재 로그인 사용자의 찜 목록을 모두 DTO로 반환 (읽기 전용이므로 트랜잭션 불필요)
    public List<RstJjimDTO> getMyFavoriteList(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return List.of();
        }
        Member member = memberRepo.findById(memberId).orElse(null);
        if (member == null) {
            return List.of();
        }
        List<RstJjim> favList = rstJjimRepo.findByMember_MemberId(memberId);
        return favList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    // 찜 추가 (DB 저장이므로 @Transactional 필요)
    @Transactional
    public String addFavorite(String memberId, Integer rstId) {
        if (memberId == null || memberId.isBlank()) {
            return "not-logged-in";
        }
        Member member = memberRepo.findById(memberId).orElse(null);
        if (member == null) {
            return "not-logged-in";
        }
        if (rstJjimRepo.existsByMember_MemberIdAndRestaurant_RstId(memberId, rstId)) {
            return "already";
        }
        Restaurant rst = restaurantRepo.findById(rstId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다. id=" + rstId));
        RstJjim entity = new RstJjim();
        entity.setMember(member);
        entity.setRestaurant(rst);
        rstJjimRepo.save(entity);
        return "added";
    }

    // 찜 해제 (delete 로직이므로 @Transactional 필요)
    @Transactional
    public String removeFavorite(String memberId, Integer rstId) {
        if (memberId == null || memberId.isBlank()) {
            return "not-logged-in";
        }
        Member member = memberRepo.findById(memberId).orElse(null);
        if (member == null) {
            return "not-logged-in";
        }
        rstJjimRepo.deleteByMember_MemberIdAndRestaurant_RstId(memberId, rstId);
        return "removed";
    }

    private RstJjimDTO toDto(RstJjim entity) {
        Restaurant rst = entity.getRestaurant();

        Integer rstJjimId      = entity.getRstJjimId();
        String memberIdValue   = entity.getMember().getMemberId();
        Integer rstIdValue     = rst.getRstId();
        String name            = rst.getName();
        String address         = rst.getAddress();
        String imagePath       = rst.getImage();         // 엔티티의 image 컬럼
        BigDecimal rating      = rst.getRating();        // 엔티티의 rating 컬럼
        String tag             = rst.getTag();           // 엔티티의 tag 컬럼
        LocalDateTime createdAt = entity.getCreatedAt();

        return new RstJjimDTO(
            rstJjimId,          // 1. rstJjimId
            memberIdValue,      // 2. memberId
            rstIdValue,         // 3. rstId
            name,               // 4. restaurantName
            address,            // 5. restaurantAddress
            imagePath,          // 6. restaurantImage
            rating,             // 7. restaurantRating
            tag,                // 8. restaurantTag
            createdAt           // 9. createdAt
        );
    }
    
 // 찜 목록을 페이지 단위로 가져오는 메서드 (무한 스크롤용)
    public List<RstJjimDTO> getMyFavoriteList(String memberId, int page) {
        if (memberId == null || memberId.isBlank()) {
            return List.of();
        }
        Member member = memberRepo.findById(memberId).orElse(null);
        if (member == null) {
            return List.of();
        }

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        List<RstJjim> pagedList = rstJjimRepo.findByMemberWithPaging(memberId, pageable);
        return pagedList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
