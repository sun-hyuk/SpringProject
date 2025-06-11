package com.dita.controller;

import com.dita.domain.Course;
import com.dita.domain.Restaurant;
import com.dita.service.CourseService;
import com.dita.dto.RestaurantDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminCoursesController {

    // CourseService를 주입 받습니다.
    @Autowired
    private CourseService courseService;

    // 관리자 코스 등록 페이지로 이동
    @GetMapping("/adminCourses")
    public String adminCoursesPage(Model model) {
        // 헤더에서 필요한 활성 메뉴 표시
        model.addAttribute("isCourseRegister", true);
        return "admin/adminCourses";
    }

    // 코스 등록 처리
    @PostMapping("/adminCourses")
    public String submitCourse(
    		@RequestParam String courseName,
                               @RequestParam String description,
                               @RequestParam(required = false) String tagsJson,
                               @RequestParam(required = false) String storesJson,
                               @RequestParam(required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) {
        try {
            if (courseName == null || courseName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "코스 이름을 입력해주세요.");
                return "redirect:/adminCourses";
            }

            if (description == null || description.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "설명을 입력해주세요.");
                return "redirect:/adminCourses";
            }

            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = courseService.saveImage(imageFile); // 이미지 저장
            }

            // Course 객체 생성
            Course course = new Course();
            course.setTitle(courseName);
            course.setIntro(description);
            course.setTag(tagsJson);
            course.setImage(imageUrl);
            
            course.setRestaurants(storesJson != null ? storesJson : "[]");

            // storesJson에서 레스토랑 목록을 파싱하여 restaurantList 생성
            List<Restaurant> restaurantList = new ArrayList<>();
            if (storesJson != null && !storesJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                restaurantList = objectMapper.readValue(storesJson, new TypeReference<List<Restaurant>>(){});
            }

            // registerCourse 메소드 호출 (Course와 List<Restaurant> 인자로)
            Course savedCourse = courseService.registerCourse(course, restaurantList);

            redirectAttributes.addFlashAttribute("successMessage", "맛집 추천 코스 등록이 완료되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "등록 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/adminCourses";
    }

    // 맛집 검색 API
    @GetMapping("/api/restaurants")
    @ResponseBody
    public List<RestaurantDTO> searchRestaurants(@RequestParam String keyword) {
        List<Restaurant> restaurants = new ArrayList<>();

        // 이름으로 검색
        restaurants.addAll(courseService.searchRestaurantsByName(keyword));

        // 태그로 검색 (중복을 피하기 위해)
        List<Restaurant> tagRestaurants = courseService.searchRestaurantsByTag(keyword);
        tagRestaurants.forEach(restaurant -> {
            if (!restaurants.contains(restaurant)) {
                restaurants.add(restaurant);
            }
        });

        // RestaurantDTO로 변환하여 반환
        List<RestaurantDTO> resultDtos = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            RestaurantDTO dto = new RestaurantDTO();
            dto.setRstId(restaurant.getRstId());
            dto.setName(restaurant.getName());
            dto.setStatus(restaurant.getStatus());
            dto.setIntro(restaurant.getIntro());
            dto.setAddress(restaurant.getAddress());
            dto.setPhone(restaurant.getPhone());
            dto.setRating(restaurant.getRating() != null ? restaurant.getRating() : BigDecimal.ZERO);
            dto.setLatitude(restaurant.getLatitude() != null ? restaurant.getLatitude() : BigDecimal.ZERO); // BigDecimal로 처리
            dto.setLongitude(restaurant.getLongitude() != null ? restaurant.getLongitude() : BigDecimal.ZERO); // BigDecimal로 처리
            dto.setTag(restaurant.getTag());
            dto.setRegionLabel(restaurant.getRegionLabel());
            dto.setRegion2Label(restaurant.getRegion2Label());
            dto.setImage(restaurant.getImage());
            dto.setJjimCount(restaurant.getJjimCount());
            dto.setCreatedAt(restaurant.getCreatedAt());
            dto.setMemberId(restaurant.getMemberId());
            resultDtos.add(dto);
        }

        return resultDtos;
    }
}
