package com.dita.controller;

import com.dita.dto.CourseDTO;
import com.dita.dto.MemberDTO;
import com.dita.service.CourseJjimService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jjim")
public class CourseJjimController {

    private final CourseJjimService jjimService;

    // ✅ 찜 추가
    @PostMapping
    public ResponseEntity<String> add(@RequestBody Map<String, Integer> request,
                                      HttpSession session) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginUser == null) return ResponseEntity.status(401).body("로그인 필요");

        Integer courseId = request.get("courseId");
        jjimService.addJjim(courseId, loginUser.getMemberId());
        return ResponseEntity.ok("added");
    }

    // ✅ 찜 삭제
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> remove(@PathVariable Integer courseId,
                                         HttpSession session) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginUser == null) return ResponseEntity.status(401).body("로그인 필요");

        jjimService.removeJjim(courseId, loginUser.getMemberId());
        return ResponseEntity.ok("removed");
    }

    // ✅ 찜 여부 확인
    @GetMapping("/{courseId}")
    public ResponseEntity<Boolean> isJjimmed(@PathVariable Integer courseId,
                                             HttpSession session) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginUser == null) return ResponseEntity.ok(false);

        boolean jjimmed = jjimService.isJjimmed(courseId, loginUser.getMemberId());
        return ResponseEntity.ok(jjimmed);
    }
    
    // 찜한 코스 전체 반환 (memberId 기준)
    @GetMapping("/list")
    public ResponseEntity<List<CourseDTO>> getMyJjims(HttpSession session) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginUser == null) return ResponseEntity.status(401).build();

        List<CourseDTO> myJjims = jjimService.getAllCourseDTOsWithJjim(loginUser.getMemberId());
        return ResponseEntity.ok(myJjims);
    }
    
 // ✅ 찜 토글 + 찜 수 반환
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam int courseId,
                                                      HttpSession session) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginUser == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 됐을 경우
        }

        boolean jjimmed = jjimService.toggleJjim(courseId, loginUser.getMemberId());
        int jjimCount = jjimService.countByCourse(courseId);

        return ResponseEntity.ok(Map.of(
            "jjimmed", jjimmed,
            "jjimCount", jjimCount
        ));
    }


}
