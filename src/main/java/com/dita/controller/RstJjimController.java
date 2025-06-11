package com.dita.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dita.dto.MemberDTO;
import com.dita.service.RstJjimService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/jjim")
public class RstJjimController {

	@Autowired
	private RstJjimService jjimService;

	@PostMapping("/toggle/{rstId}")
        public Map<String, Object> toggleJjim(@PathVariable int rstId, HttpSession session) {
             MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
             if (member == null) {
                 throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
             }

             String memberId = member.getMemberId();
             
             boolean added = jjimService.toggleJjim(memberId, rstId);
                     // 변경된 전체 찜 카운트를 다시 조회
                   int newCount = jjimService.countJjim(rstId);
                   // result + count 동시 반환
                     return Map.of(
                         "result", added ? "added" : "removed",
                         "count", newCount
                     );
}
}
