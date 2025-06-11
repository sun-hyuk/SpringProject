package com.dita.service;

import com.dita.domain.RstVisited;
import com.dita.dto.RstVisitedDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.persistence.RstVisitedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RstVisitedService {
    private final RstVisitedRepository visitedRepo;
    private final RestaurantService restaurantService;

    @Transactional
    public void recordVisit(String memberId, Integer rstId) {
        visitedRepo.upsertVisit(memberId, rstId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<RstVisitedDTO> getRecentVisits(String memberId) {
        return visitedRepo
            .findTop10ByMemberIdOrderByVisitedAtDesc(memberId)
            .stream()
            // 람다 파라미터 타입을 명시하면 더 안전합니다
            .map((RstVisited v) -> {
                // 1) 식당 정보 조회
                RestaurantDTO r = restaurantService.getRestaurantById(v.getRestaurantId());
                // 2) DTO 빌드 및 반환 (반드시 return!)
                return RstVisitedDTO.builder()
                    .visitedId(v.getVisitedId())
                    .memberId(v.getMemberId())
                    .restaurantId(v.getRestaurantId())
                    .visitedAt(v.getVisitedAt())
                    .restaurantName(r.getName())
                    .thumbnailUrl(r.getImage())
                    .averageRating(r.getRating())
                    .region(r.getRegionLabel())
                    
                    .build();
            })
            .collect(Collectors.toList());
    }
}
