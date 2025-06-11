package com.dita.controller;

import com.dita.domain.Course;
import com.dita.dto.CourseApiDTO;
import com.dita.dto.CourseDTO;
import com.dita.dto.MemberDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.service.CourseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseApiController {

    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    // 전체 코스 목록
    @GetMapping
    public List<CourseApiDTO> list() throws Exception {
        return courseService.getAllCourses().stream()
                .map(c -> {
                    List<RestaurantDTO> stores;
                    try {
                        stores = objectMapper.readValue(c.getRestaurants(),
                                new TypeReference<List<RestaurantDTO>>() {});
                    } catch (Exception ex) {
                        stores = List.of();
                    }
                    return CourseApiDTO.fromEntity(c, stores);
                }).collect(Collectors.toList());
    }

    // 단일 코스 상세
    @GetMapping("/{id}")
    public CourseApiDTO detail(@PathVariable Integer id) throws Exception {
    	Course c = courseService.getCourseEntityById(id);
        List<RestaurantDTO> stores;
        try {
            stores = objectMapper.readValue(c.getRestaurants(),
                    new TypeReference<List<RestaurantDTO>>() {});
        } catch (Exception ex) {
            stores = List.of();
        }
        return CourseApiDTO.fromEntity(c, stores);
    }

    // 로그인한 회원 기준 찜 여부 포함된 리스트
    @GetMapping("/member")
    public List<CourseDTO> memberCourses(HttpSession session) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        String memberId = (loginUser != null) ? loginUser.getMemberId() : null;
        return courseService.getAllCourseDTOsWithJjim(memberId);
    }
}
