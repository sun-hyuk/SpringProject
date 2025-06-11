package com.dita.service;

import com.dita.domain.Alerts;
import com.dita.domain.Member;
import com.dita.dto.AlertsDTO;
import com.dita.dto.MemberDTO;
import com.dita.persistence.AlertsRepository;
import com.dita.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertsService {

    private final AlertsRepository alertsRepository;
    private final MemberRepository memberRepository;

    // 로그인된 회원 정보 조회
    public MemberDTO getLoggedInMemberInfo(String memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null) return null;

        MemberDTO dto = new MemberDTO();
        dto.setMemberId(member.getMemberId());
        dto.setNickname(member.getNickname());
        dto.setImage(member.getImage());
        return dto;
    }

    // 특정 회원의 알림 전체 조회 (MemberDTO 기반)
    public List<AlertsDTO> getAlertsByMember(MemberDTO memberDto) {
        return getAlertsByMember(memberDto.getMemberId());
    }

    // 특정 회원의 알림 전체 조회 (memberId 기반)
    public List<AlertsDTO> getAlertsByMember(String memberId) {
        List<Alerts> alertsList = alertsRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId);

        return alertsList.stream()
                .map(alert -> {
                    AlertsDTO dto = new AlertsDTO();
                    dto.setAlertsId(alert.getAlertsId());
                    dto.setIsRead(alert.getIsRead());
                    dto.setType(alert.getType().name());
                    dto.setTargetId(alert.getTargetId());
                    dto.setMemberId(memberId);
                    dto.setCreatedAt(alert.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 특정 회원의 알림 중 타입별 필터링 (memberId + type 기반)
    public List<AlertsDTO> getAlertsByMemberAndType(String memberId, String type) {
        return getAlertsByMember(memberId).stream()
                .filter(alert -> alert.getType().equals(type))
                .collect(Collectors.toList());
    }

    // 알림 읽음 처리
    public void markAsRead(Integer alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다."));
        alert.setIsRead(true);
        alertsRepository.save(alert);
    }
    
    // 알림 생성
    public void createAlert(String memberId, Alerts.AlertType type, Integer targetId) {
        // 알림 대상 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Alerts alert = new Alerts();
        alert.setMember(member);
        alert.setType(type);
        alert.setTargetId(targetId);
        alert.setIsRead(false);  // 초기 상태는 읽지 않음
        alert.setCreatedAt(new Date());

        alertsRepository.save(alert);
    }
    
    // 알림 삭제
    public void deleteById(Integer alertId) {
        alertsRepository.deleteById(alertId);
    }
    
    // 읽지 않은 알림이 있는지 확인
    public boolean hasUnreadAlerts(String memberId) {
        return alertsRepository.existsByMember_MemberIdAndIsReadFalse(memberId);
    }
    
    public void markAllAsRead(String memberId) {
        List<Alerts> unreadAlerts = alertsRepository.findByMember_MemberIdAndIsReadFalse(memberId);
        for (Alerts alert : unreadAlerts) {
            alert.setIsRead(true);
        }
        alertsRepository.saveAll(unreadAlerts);
    }
}
