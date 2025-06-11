package com.dita.controller;

import com.dita.dto.AlertsDTO;
import com.dita.dto.MemberDTO;
import com.dita.persistence.ReportRepository;
import com.dita.persistence.ReviewRepository;
import com.dita.service.AlertsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/alerts")
public class AlertsController {

    private final AlertsService alertsService;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    @GetMapping
    public String showAlertsPage(@RequestParam(value = "type", required = false, defaultValue = "전체") String type,
                                 HttpSession session, Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");

        if (memberDto == null) {
            model.addAttribute("loggedInMember", null);
            model.addAttribute("alertsGroupedByDate", Map.of());
            model.addAttribute("alertMessageMap", Map.of());
            model.addAttribute("activeType", type);
            return "alerts";
        }

        // 전체 또는 타입별 알림 조회
        List<AlertsDTO> alerts = type.equals("전체")
                ? alertsService.getAlertsByMember(memberDto.getMemberId())
                : alertsService.getAlertsByMember(memberDto.getMemberId()).stream()
                    .filter(a -> a.getType().equals(type))
                    .toList();

        // 날짜별 그룹핑 (yyyy-MM-dd 기준)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<AlertsDTO>> grouped = alerts.stream()
                .collect(Collectors.groupingBy(
                        dto -> sdf.format(dto.getCreatedAt()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // alertId → 메시지 맵
        Map<Integer, String> alertMessageMap = new HashMap<>();
        for (AlertsDTO alert : alerts) {
        	if (alert.getType().equals("신고")) {
        	    Integer targetId = alert.getTargetId();

        	    if (reportRepository.existsById(targetId)) {
        	        // 신고 완료 알림 (targetId = 신고 ID)
        	        alertMessageMap.put(alert.getAlertsId(), "신고가 처리되었습니다.");
        	    }
        	    else if (reviewRepository.existsById(targetId)) {
        	        // 리뷰 관련 알림 (targetId = 리뷰 ID)

        	        // 이 리뷰의 작성자 == 로그인된 사용자 → 피신고자
        	        reviewRepository.findById(targetId).ifPresentOrElse(review -> {
        	            if (review.getMember().getMemberId().equals(memberDto.getMemberId())) {
        	                // 내가 작성한 리뷰 → 피신고자
        	                alertMessageMap.put(alert.getAlertsId(), "내 리뷰가 신고되었습니다.");
        	            } else {
        	                // 그 외 → 신고자
        	                alertMessageMap.put(alert.getAlertsId(), "신고가 접수되었습니다.");
        	            }
        	        }, () -> {
        	            alertMessageMap.put(alert.getAlertsId(), "신고 관련 알림입니다.");
        	        });
        	    }
        	    else {
        	        alertMessageMap.put(alert.getAlertsId(), "알 수 없는 신고 알림입니다.");
        	    }
        	}

        }

        model.addAttribute("loggedInMember", memberDto);
        model.addAttribute("alertsGroupedByDate", grouped);
        model.addAttribute("alertMessageMap", alertMessageMap);
        model.addAttribute("activeType", type);
        return "alerts";
    }

    @PostMapping("/read/{id}")
    @ResponseBody
    public String markAsRead(@PathVariable("id") Integer alertId) {
        alertsService.markAsRead(alertId);
        return "OK";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<AlertsDTO> getAlertsByType(@PathVariable("type") String type, HttpSession session) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto == null) return Collections.emptyList();
        return alertsService.getAlertsByMemberAndType(memberDto.getMemberId(), type);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteAlert(@PathVariable("id") Integer alertId) {
        alertsService.deleteById(alertId);
        return "OK";
    }

    @GetMapping("/unread")
    @ResponseBody
    public Map<String, Boolean> hasUnreadAlerts(HttpSession session) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        boolean hasUnread = false;
        if (memberDto != null) {
            hasUnread = alertsService.hasUnreadAlerts(memberDto.getMemberId());
        }
        return Map.of("hasUnread", hasUnread);
    }

    @PostMapping("/read/all")
    @ResponseBody
    public String markAllAsRead(HttpSession session) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto != null) {
            alertsService.markAllAsRead(memberDto.getMemberId());
        }
        return "OK";
    }
}
