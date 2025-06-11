package com.dita.persistence;

import com.dita.domain.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertsRepository extends JpaRepository<Alerts, Integer> {

    // 특정 회원의 알림 내역 조회 (최신순)
    List<Alerts> findByMember_MemberIdOrderByCreatedAtDesc(String memberId);

    // 읽지 않은 알림 조회
    List<Alerts> findByMember_MemberIdAndIsReadFalse(String memberId);
    
    boolean existsByMember_MemberIdAndIsReadFalse(String memberId);
}
