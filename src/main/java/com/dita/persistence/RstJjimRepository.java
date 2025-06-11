// src/main/java/com/dita/persistence/RstJjimRepository.java
package com.dita.persistence;

import com.dita.domain.Member;
import com.dita.domain.Restaurant;
import com.dita.domain.RstJjim;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RstJjimRepository extends JpaRepository<RstJjim, Integer> {

    // 특정 Member가 찜한 모든 RstJjim 목록
	List<RstJjim> findByMember_MemberId(String memberId);

    // 중복 찜 방지를 위해 이미 존재하는지 확인
	boolean existsByMember_MemberIdAndRestaurant_RstId(String memberId, Integer rstId);

    // 찜 해제 시 사용: Member와 해당 rstId 로 삭제
	void deleteByMember_MemberIdAndRestaurant_RstId(String memberId, Integer rstId);
	
    // ★ 찜 카운트를 조회하는 메서드 추가
	int countByRestaurantRstId(Integer rstId);
	
	/**
     * Member와 Restaurant 객체로 RstJjim 조회 (Optional로 감싸 예외 방지)
     * @param member 회원 객체
     * @param restaurant 레스토랑 객체
     * @return Optional<RstJjim>
     */
    Optional<RstJjim> findByMemberAndRestaurant(Member member, Restaurant restaurant);

    /**
     * Member와 Restaurant 객체 기준으로 찜 여부 존재 확인
     * @param member 회원 객체
     * @param restaurant 레스토랑 객체
     * @return 찜한 상태면 true, 아니면 false
     */
    boolean existsByMemberAndRestaurant(Member member, Restaurant restaurant);
    
    @Query("SELECT r FROM RstJjim r WHERE r.member.memberId = :memberId ORDER BY r.createdAt DESC")
    List<RstJjim> findByMemberWithPaging(
        @Param("memberId") String memberId,
        Pageable pageable
    );



}
