	package com.dita.service;
	
	import com.dita.domain.Alerts.AlertType;
	import com.dita.domain.Restaurant;
	import com.dita.dto.RestaurantDTO;
	import com.dita.persistence.RestaurantRepository;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.data.domain.*;
	import org.springframework.data.jpa.domain.Specification;
	import org.springframework.stereotype.Service;
	
	import jakarta.persistence.criteria.Expression;
	import jakarta.persistence.criteria.Path;
	import jakarta.persistence.criteria.Predicate;
	import java.time.LocalDateTime;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.stream.Collectors;
	
	@Service
	public class RestaurantService {
	
	    private final AlertsService alertsService;
	
	    private final RestaurantRepository restaurantRepository;
	
	    @Autowired
	    public RestaurantService(RestaurantRepository restaurantRepository, AlertsService alertsService) {
	        this.restaurantRepository = restaurantRepository;
	        this.alertsService = alertsService;
	    }
	
	    /**
	     * 모든 맛집(restaurant) 목록을 DTO로 변환하여 반환
	     */
	    public List<RestaurantDTO> getAllRestaurants() {
	        List<Restaurant> entities = restaurantRepository.findAll();
	        return entities.stream()
	                .map(this::toDto)
	                .collect(Collectors.toList());
	    }
	
	    /**
	     * 식당 이름으로 LIKE 검색
	     */
	    public List<RestaurantDTO> searchByName(String keyword) {
	        List<Restaurant> entities = restaurantRepository.findByNameContaining(keyword);
	        return entities.stream()
	                .map(this::toDto)
	                .collect(Collectors.toList());
	    }
	
	    /**
	     * 페이징 가능한 “최신 등록순” 조회 (승인 상태만)
	     */
	    public Page<RestaurantDTO> getRecentRestaurantsPage(int page, int size) {
	        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
	        Specification<Restaurant> spec = (root, query, cb) ->
	                cb.equal(root.get("status"), "승인");
	        Page<Restaurant> pageEntity = restaurantRepository.findAll(spec, pageable);
	        return pageEntity.map(this::toDto);
	    }
	
	    /**
	     * 최신순으로 상위 4개 식당을 RestaurantDTO 리스트로 반환 (승인 상태만)
	     */
	    public List<RestaurantDTO> getRecentRestaurants() {
	        // 페이징(0번 페이지, 4개, 최신순), status='승인' 조건 적용
	        Pageable pageable = PageRequest.of(0, 4, Sort.by("createdAt").descending());
	        Specification<Restaurant> spec = (root, query, cb) ->
	                cb.equal(root.get("status"), "승인");
	        Page<Restaurant> recentPage = restaurantRepository.findAll(spec, pageable);
	        return recentPage.getContent().stream()
	                .map(this::toDto)
	                .collect(Collectors.toList());
	    }
	
	    /**
	     * 페이징된 전체 조회: PageRequest.of(page, size, Sort.by("rstId").descending()) 예시
	     */
	    public Page<RestaurantDTO> getRestaurantPage(int page, int size) {
	        Pageable pageable = PageRequest.of(page, size, Sort.by("rstId").descending());
	        Page<Restaurant> entityPage = restaurantRepository.findAll(pageable);
	        return entityPage.map(this::toDto);
	    }
	
	    /**
	     * 페이징된 이름 검색: PageRequest.of(page, size, Sort.by("rstId").descending()) 예시
	     */
	    public Page<RestaurantDTO> searchByNamePage(String keyword, int page, int size) {
	        Pageable pageable = PageRequest.of(page, size, Sort.by("rstId").descending());
	        Page<Restaurant> entityPage = restaurantRepository.findByNameContaining(keyword, pageable);
	        return entityPage.map(this::toDto);
	    }
	
	    /**
	     * 상태(status)로 조회 (예: '승인'된 식당만)
	     */
	    public List<RestaurantDTO> getByStatus(String status) {
	        List<Restaurant> entities = restaurantRepository.findByStatus(status);
	        return entities.stream()
	                .map(this::toDto)
	                .collect(Collectors.toList());
	    }
	
	    /**
	     * PK(rstId)로 단건 조회
	     */
	    public RestaurantDTO getRestaurantById(Integer rstId) {
	        return restaurantRepository.findById(rstId)
	                .map(this::toDto)
	                .orElse(null);
	    }
	
	    /**
	     * 이름(name)으로 단건 조회
	     */
	    public RestaurantDTO getRestaurantByName(String name) {
	        return restaurantRepository.findByNameContaining(name)
	                .stream()
	                .findFirst()
	                .map(this::toDto)
	                .orElse(null);
	    }
	    
	    // ─────────────────────────────────────────────────────────────────────────
	    //  여기에 searchWithFilters(...) 메서드 추가
	    // ─────────────────────────────────────────────────────────────────────────
	
	    /**
	     * 검색어(keyword), 대분류 지역(region), 상세지역 리스트(detailList),
	     * 음식 종류 리스트(typeList), 신상 가게 조건(newStore) + 페이징(pageable)
	     * 을 조합하여 Page<RestaurantDTO> 를 반환
	     *
	     * @param keyword    식당 이름 검색 키워드 (like)
	     * @param region     대분류 지역명 (e.g. "서울", "부산", "제주시", 또는 "전체")
	     * @param detailList 소분류(region_label) 목록 (e.g. ["강남구"], ["해운대구"] 등)
	     * @param typeList   음식 종류 목록 (e.g. ["한식","양식"])
	     * @param newStore   신상 가게 조건 (e.g. "오픈 1개월 이내", "오픈 3개월 이내", "최근 등록")
	     * @param pageable   페이징 정보
	     * @return Page<RestaurantDTO>
	     */
	    public Page<RestaurantDTO> searchWithFilters(
	            String keyword,
	            String region,
	            List<String> detailList,
	            List<String> typeList,
	            String newStore,
	            Pageable pageable) {
	
	        // 1) 기본 Specification 시작: status = '승인'
	        Specification<Restaurant> spec = (root, query, cb) ->
	                cb.equal(root.get("status"), "승인");
	
	        // 1-1) 검색어(keyword) 조건: name LIKE %keyword% OR intro LIKE %keyword%
	        if (keyword != null && !keyword.isBlank()) {
	            spec = spec.and((root, query, builder) -> {
	                String pattern = "%" + keyword.trim() + "%";
	                Predicate byName  = builder.like(root.get("name"), pattern);
	                Predicate byIntro = builder.like(root.get("intro"), pattern);
	                Predicate byTag   = builder.like(root.get("tag"), pattern);
	                return builder.or(byName, byIntro, byTag);
	            });
	        }
	
	        // 2) 지역(대분류: address, 소분류: regionLabel) 필터
	        if (region != null && !region.isBlank() && !"전체".equals(region)) {
	            // 2-1) 소분류(detailList)가 있으면: address LIKE '%region%' AND regionLabel = detail
	            if (detailList != null && !detailList.isEmpty()) {
	                String detail = detailList.get(0).trim(); // 예: "강남구", "해운대구"
	                spec = spec.and((root, query, builder) -> {
	                    List<Predicate> predicates = new ArrayList<>();
	
	                    // (A) address(대분류) 포함 조건
	                    predicates.add(
	                        builder.like(
	                            builder.lower(root.get("address")),
	                            "%" + region.toLowerCase().trim() + "%"
	                        )
	                    );
	
	                    // (B) regionLabel(소분류)과 정확히 일치
	                    predicates.add(
	                        builder.equal(
	                            builder.lower(root.get("regionLabel")),
	                            detail.toLowerCase()
	                        )
	                    );
	
	                    return builder.and(predicates.toArray(new Predicate[0]));
	                });
	            }
	            // 2-2) detailList가 비어 있으면: address LIKE '%region%' 만
	            else {
	                spec = spec.and((root, query, builder) ->
	                    builder.like(
	                        builder.lower(root.get("address")),
	                        "%" + region.toLowerCase().trim() + "%"
	                    )
	                );
	            }
	        }
	
	        // 3) 음식 종류(typeList) 조건: tag 컬럼에 LIKE %type% 중 하나라도 포함
	        if (typeList != null && !typeList.isEmpty()) {
	            spec = spec.and((root, query, builder) -> {
	                Path<String> tagPath = root.get("tag");
	                Predicate combined = null;
	                for (String type : typeList) {
	                    Predicate p = builder.like(tagPath, "%" + type + "%");
	                    combined = (combined == null) ? p : builder.or(combined, p);
	                }
	                return combined;
	            });
	        }
	
	        // 4) 신상 가게(newStore) 조건: createdAt > (지금 - 기간)
	        if (newStore != null && !newStore.isBlank()) {
	            LocalDateTime now    = LocalDateTime.now();
	            LocalDateTime cutoff = null;
	            switch (newStore) {
	                case "오픈 1개월 이내":
	                    cutoff = now.minusMonths(1);
	                    break;
	                case "오픈 3개월 이내":
	                    cutoff = now.minusMonths(3);
	                    break;
	                case "최근 등록":
	                    cutoff = now.minusDays(7);
	                    break;
	                default:
	                    cutoff = null;
	            }
	            if (cutoff != null) {
	                LocalDateTime finalCutoff = cutoff;
	                spec = spec.and((root, query, builder) ->
	                    builder.greaterThan(root.get("createdAt"), finalCutoff)
	                );
	            }
	        }
	
	        // 5) JpaSpecificationExecutor 에서 제공하는 findAll(spec, pageable) 호출
	        Page<Restaurant> entityPage = restaurantRepository.findAll(spec, pageable);
	
	        // 6) Page<Restaurant> → Page<RestaurantDTO> 로 변환
	        return entityPage.map(this::toDto);
	    }
	
	    // ─────────────────────────────────────────────────────────────────────────
	    //  엔티티 → DTO 변환 헬퍼 메서드
	    // ─────────────────────────────────────────────────────────────────────────
	    private RestaurantDTO toDto(Restaurant entity) {
	        RestaurantDTO dto = new RestaurantDTO();
	        dto.setRstId(entity.getRstId());
	        dto.setName(entity.getName());
	        dto.setStatus(entity.getStatus());
	        dto.setIntro(entity.getIntro());
	        dto.setAddress(entity.getAddress());
	        dto.setPhone(entity.getPhone());
	        dto.setRating(entity.getRating());
	        dto.setLatitude(entity.getLatitude());
	        dto.setLongitude(entity.getLongitude());
	        dto.setTag(entity.getTag());
	        dto.setRegionLabel(entity.getRegionLabel());
	        dto.setRegion2Label(entity.getRegion2Label());
	        dto.setImage(entity.getImage());
	        dto.setJjimCount(entity.getJjimCount());
	        dto.setCreatedAt(entity.getCreatedAt());
	        dto.setMemberId(entity.getMemberId());
	        return dto;
	    }
	
	    /**
	     * 새로운 가게 등록 (기본 상태는 '대기')
	     */
	    public void save(Restaurant restaurant) {
	        if (restaurant.getStatus() == null || restaurant.getStatus().isBlank()) {
	            restaurant.setStatus("대기");
	        }
	        restaurantRepository.save(restaurant);
	    }
	
	    /**
	     * 승인된 가게 목록 조회 (멤버별)
	     */
	    public List<Restaurant> findApprovedStoresByMemberId(String memberId) {
	        return restaurantRepository.findByMemberIdAndStatus(memberId, "승인");
	    }
	
	    /**
	     * 승인 대기중인 가게 목록 조회 (멤버별)
	     */
	    public List<Restaurant> findPendingStoresByMemberId(String memberId) {
	        return restaurantRepository.findByMemberIdAndStatus(memberId, "대기");
	    }
	
	    public Restaurant findById(int rstId) {
	        return restaurantRepository.findById(rstId).orElse(null);
	    }
	
	    /**
	     * 기존 가게 정보 수정
	     */
	    public void update(Restaurant updated) {
	        Restaurant original = restaurantRepository.findById(updated.getRstId()).orElse(null);
	        if (original == null) return;
	
	        // 필드별 업데이트
	        original.setName(updated.getName());
	        original.setAddress(updated.getAddress());
	        original.setLatitude(updated.getLatitude());
	        original.setLongitude(updated.getLongitude());
	        original.setRegionLabel(updated.getRegionLabel());
	        original.setRegion2Label(updated.getRegion2Label());
	        original.setIntro(updated.getIntro());
	        original.setPhone(updated.getPhone());
	        original.setTag(updated.getTag());
	
	        // 이미지가 새로 업로드된 경우만 변경
	        if (updated.getImage() != null && !updated.getImage().isBlank()) {
	            original.setImage(updated.getImage());
	        }
	
	        restaurantRepository.save(original);
	    }
	
	    // ─────────────────────────────────────────────────────────────────────────
	    // 관리자용 -----------------------------------------------------------------------------------
	    // ─────────────────────────────────────────────────────────────────────────
	
	    /**
	     * 관리자 가게등록 - 승인된 목록 (검색 + 페이징)
	     */
	    public Page<RestaurantDTO> getApprovedRestaurantsPage(String searchType, String keyword, Pageable pageable) {
	        Specification<Restaurant> spec = (root, query, builder) ->
	                builder.equal(root.get("status"), "승인");
	
	        if (keyword != null && !keyword.isBlank()) {
	            String pattern = "%" + keyword.trim() + "%";
	
	            if ("name".equals(searchType)) {
	                // 이름에만 필터링
	                spec = spec.and((root1, query1, builder1) ->
	                        builder1.like(root1.get("name"), pattern));
	            } else if ("location".equals(searchType)) {
	                spec = spec.and((root, query1, builder1) -> {
	                    Expression<String> fullRegion =
	                            builder1.concat(root.get("regionLabel"), root.get("region2Label"));
	                    return builder1.like(fullRegion, pattern);
	                });
	            } else if ("all".equals(searchType) || searchType == null || searchType.isBlank()) {
	                spec = spec.and((root1, query1, builder1) -> {
	                    Predicate nameLike = builder1.like(root1.get("name"), pattern);
	                    Expression<String> fullRegion =
	                            builder1.concat(root1.get("regionLabel"), root1.get("region2Label"));
	                    Predicate regionLike = builder1.like(fullRegion, pattern);
	                    return builder1.or(nameLike, regionLike);
	                });
	            }
	        }
	
	        return restaurantRepository.findAll(spec, pageable).map(this::toDto);
	    }
	
	    /**
	     * 관리자용: 승인 대기중인 가게 목록 조회 (페이징)
	     */
	    public Page<RestaurantDTO> getPendingRestaurantsPage(Pageable pageable) {
	        Specification<Restaurant> spec = (root, query, builder) ->
	                builder.equal(root.get("status"), "대기");
	        return restaurantRepository.findAll(spec, pageable).map(this::toDto);
	    }
	
	    /**
	     * 관리자용: 가게 승인 처리
	     */
	    public void approveRestaurant(int rstId) {
	        Restaurant restaurant = restaurantRepository.findById(rstId).orElse(null);
	        if (restaurant != null) {
	            restaurant.setStatus("승인");
	            restaurantRepository.save(restaurant);
	            
	            // 승인된 회원에게 '식당' 알림 발송
	            alertsService.createAlert(
	                restaurant.getMemberId(),   // 알림 받을 회원
	                AlertType.식당,             // 알림 유형: enum 값
	                restaurant.getRstId()       // 가게 ID (targetId)
	            );
	        }
	    }
	
	    /**
	     * 관리자용: 가게 상세 조회 (DTO)
	     */
	    public RestaurantDTO findRestaurantDetailById(int rstId) {
	        return restaurantRepository.findById(rstId)
	                .map(this::toDto)
	                .orElse(null);
	    }
	
	    /**
	     * 관리자용: 가게 삭제
	     */
	    public void deleteById(int rstId) {
	        restaurantRepository.deleteById(rstId);
	    }
	    
	    // 관리자에서 코스등록
	    public List<RestaurantDTO> searchRestaurants(String keyword) {
	        List<Restaurant> restaurants = restaurantRepository.findByNameContainingIgnoreCase(keyword);
	        return restaurants.stream()
	                .map(this::toDto)
	                .collect(Collectors.toList());
	    }
	    
	    public List<RestaurantDTO> searchWithFiltersWithoutPage(
	            String keyword,
	            String region,
	            List<String> detailList,
	            List<String> typeList,
	            String newStore) {

	        Specification<Restaurant> spec = (root, query, cb) ->
	                cb.equal(root.get("status"), "승인");

	        if (keyword != null && !keyword.isBlank()) {
	            spec = spec.and((root, query1, builder) -> {
	                String pattern = "%" + keyword.trim() + "%";
	                Predicate byName = builder.like(root.get("name"), pattern);
	                Predicate byIntro = builder.like(root.get("intro"), pattern);
	                Predicate byTag = builder.like(root.get("tag"), pattern);
	                return builder.or(byName, byIntro, byTag);
	            });
	        }

	        if (region != null && !region.isBlank() && !"전체".equals(region)) {
	            if (detailList != null && !detailList.isEmpty()) {
	                String detail = detailList.get(0).trim();
	                spec = spec.and((root, query, builder) -> {
	                    List<Predicate> predicates = new ArrayList<>();
	                    predicates.add(builder.like(builder.lower(root.get("address")), "%" + region.toLowerCase().trim() + "%"));
	                    predicates.add(builder.equal(builder.lower(root.get("regionLabel")), detail.toLowerCase()));
	                    return builder.and(predicates.toArray(new Predicate[0]));
	                });
	            } else {
	                spec = spec.and((root, query, builder) ->
	                        builder.like(builder.lower(root.get("address")), "%" + region.toLowerCase().trim() + "%"));
	            }
	        }

	        if (typeList != null && !typeList.isEmpty()) {
	            spec = spec.and((root, query, builder) -> {
	                Path<String> tagPath = root.get("tag");
	                Predicate combined = null;
	                for (String type : typeList) {
	                    Predicate p = builder.like(tagPath, "%" + type + "%");
	                    combined = (combined == null) ? p : builder.or(combined, p);
	                }
	                return combined;
	            });
	        }

	        if (newStore != null && !newStore.isBlank()) {
	            LocalDateTime now = LocalDateTime.now();
	            LocalDateTime cutoff = null;
	            switch (newStore) {
	                case "오픈 1개월 이내":
	                    cutoff = now.minusMonths(1);
	                    break;
	                case "오픈 3개월 이내":
	                    cutoff = now.minusMonths(3);
	                    break;
	                case "최근 등록":
	                    cutoff = now.minusDays(7);
	                    break;
	            }
	            if (cutoff != null) {
	                LocalDateTime finalCutoff = cutoff;
	                spec = spec.and((root, query, builder) ->
	                        builder.greaterThan(root.get("createdAt"), finalCutoff));
	            }
	        }

	        List<Restaurant> entities = restaurantRepository.findAll(spec);
	        return entities.stream().map(this::toDto).collect(Collectors.toList());
	    }
	}
