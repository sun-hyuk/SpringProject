package com.dita.dto;

import com.dita.domain.Course;
import lombok.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseApiDTO {
    private Integer course_id;
    private String title;
    private String intro;
    private List<RestaurantDTO> restaurants;
    private Integer jjimCount;
    private Integer likes;
    private String image;
    private String createAt;  // "2025년 6월 7일" 포맷

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    /** 엔티티 → API DTO 변환 */
    public static CourseApiDTO fromEntity(Course c, List<RestaurantDTO> storeDtos) {
        CourseApiDTO dto = new CourseApiDTO();
        dto.setCourse_id(c.getCourse_id());
        dto.setTitle(c.getTitle());
        dto.setIntro(c.getIntro());
        dto.setRestaurants(storeDtos);
        dto.setJjimCount(c.getJjim_count());
        dto.setLikes(c.getLikes());
        dto.setImage(c.getImage());
        dto.setCreateAt(c.getCreate_at().format(FMT));
        return dto;
    }
}
