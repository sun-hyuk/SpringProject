package com.dita.service;

import com.dita.domain.Course;
import com.dita.domain.Member;
import com.dita.domain.Restaurant;
import com.dita.dto.CourseDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.persistence.CourseJjimRepository;
import com.dita.persistence.CourseRepository;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.RestaurantRepository;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

@Service
public class CourseService {

	@Value("${app.upload.dir}")
    private String uploadDir;
	
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private final RestaurantRepository restaurantRepository;
    private final CourseRepository courseRepository;
    private final CourseJjimRepository courseJjimRepository;
    private final MemberRepository memberRepository;
    
    
    // 생성자 주입을 사용하여 두 repository 모두 주입 받기
    public CourseService(RestaurantRepository restaurantRepository,
			            CourseRepository courseRepository,
			            CourseJjimRepository courseJjimRepository,
			            MemberRepository memberRepository) {
			this.restaurantRepository = restaurantRepository;
			this.courseRepository = courseRepository;
			this.courseJjimRepository = courseJjimRepository;
			this.memberRepository = memberRepository;
			}
    
   
    
    // 코스 등록
    public Course registerCourse(Course course, List<Restaurant> selectedRestaurants) {
        // 음식점 목록에서 이름만 추출하여 restaurants 필드에 저장
        StringBuilder restaurantNames = new StringBuilder();
        for (Restaurant restaurant : selectedRestaurants) {
            restaurantNames.append(restaurant.getName()).append(", ");
        }

        // 마지막에 추가된 ", "를 제거
        if (restaurantNames.length() > 0) {
            restaurantNames.setLength(restaurantNames.length() - 2); // ", "를 제거
        }

        // 음식점 이름만 저장
        course.setRestaurants(restaurantNames.toString());

        // course 저장 로직
        return courseRepository.save(course);  // Course 객체를 반환
    }
    
    public List<CourseDTO> getAllCourseDTOs() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
        return courseRepository.findAll().stream()
            .map(c -> new CourseDTO(
                c.getCourse_id(),
                c.getTitle(),
                c.getIntro(),
                c.getTag(),
                c.getRestaurants(),
                c.getJjim_count(),
                c.getLikes(),
                c.getImage(),
                c.getCreate_at().format(fmt)
            ))
            .collect(Collectors.toList());
    }

        public String saveImage(MultipartFile imageFile) throws IOException {
            if (imageFile == null || imageFile.isEmpty()) {
                logger.warn("saveImage 호출했으나 imageFile 이 null 이거나 비어있음");
                return null;
            }

            logger.debug("app.upload.dir = {}", uploadDir);
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            logger.debug("실제 업로드 경로 = {}", uploadPath);

            if (Files.notExists(uploadPath)) {
                logger.info("업로드 디렉터리가 없어 생성: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            String original = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String ext      = StringUtils.getFilenameExtension(original);
            String base     = FilenameUtils.getBaseName(original);
            logger.debug("원본 파일명='{}', base='{}', ext='{}'", original, base, ext);

            String filename = base + "_" + UUID.randomUUID() + (ext != null && !ext.isBlank() ? "." + ext : "");
            Path targetFile = uploadPath.resolve(filename);
            logger.debug("저장 대상 파일 = {}", targetFile);

            try (InputStream in = imageFile.getInputStream()) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                logger.info("파일 저장 완료: {}", targetFile);
            } catch (IOException e) {
                logger.error("파일 저장 실패: {}", e.getMessage(), e);
                throw e;
            }

            String url = "/uploads/" + filename;
            logger.debug("반환 URL = {}", url);
            return url;
        }

    // 이름으로 레스토랑 검색
    public List<Restaurant> searchRestaurantsByName(String keyword) {
        return restaurantRepository.findByNameContainingIgnoreCase(keyword);
    }

    // 태그로 레스토랑 검색
    public List<Restaurant> searchRestaurantsByTag(String keyword) {
        return restaurantRepository.findByTagContainingIgnoreCase(keyword);
    }
    
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public CourseDTO getCourseById(int courseId) {
        Course course = courseRepository.findById(courseId)
                          .orElseThrow(() -> new IllegalArgumentException("코스 없음"));
        return convertToDTO(course); // 👉 Course를 CourseDTO로 변환
    }
    
    public Optional<Course> findById(int id) {
    	  return courseRepository.findById(id);
    	}

    public List<CourseDTO> getAllCourseDTOsWithJjim(String memberId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

        final Member member = (memberId != null)
            ? memberRepository.findById(memberId).orElse(null)
            : null;

        List<Course> courseList = courseRepository.findAll();
        return courseList.stream().map(c -> {
            boolean jjimmed = false;
            if (member != null) {
                jjimmed = courseJjimRepository.findByMemberAndCourse(member, c).isPresent();
            }

            CourseDTO dto = new CourseDTO(
                c.getCourse_id(),
                c.getTitle(),
                c.getIntro(),
                c.getTag(),
                c.getRestaurants(),
                c.getJjim_count(),
                c.getLikes(),
                c.getImage(),
                c.getCreate_at().format(fmt)
            );
            dto.setJjimmed(jjimmed);
            return dto;
        }).collect(Collectors.toList());
    }

    public CourseDTO convertToDTO(Course c) {
        return new CourseDTO(
            c.getCourse_id(),
            c.getTitle(),
            c.getIntro(),
            c.getTag(),
            c.getRestaurants(),
            c.getJjim_count(),
            c.getLikes(),
            c.getImage(),
            c.getCreate_at().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
        );
    }
    
    public List<RestaurantDTO> getRestaurantsByCourseId(int courseId) {
        // 1. Course 엔티티 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));

        // 2. 문자열로 저장된 레스토랑 이름을 ,로 나눠 리스트로 변환
        String restaurantNamesStr = course.getRestaurants(); // "식당1, 식당2"
        if (restaurantNamesStr == null || restaurantNamesStr.isBlank()) {
            return List.of(); // 아무것도 없으면 빈 리스트 반환
        }

        List<String> names = List.of(restaurantNamesStr.split("\\s*,\\s*")); // 공백 제거 후 split

        // 3. 이름으로 레스토랑 엔티티 검색
        List<Restaurant> restaurants = restaurantRepository.findByNameIn(names);

        // 4. 엔티티를 DTO로 변환해서 반환
        return restaurants.stream().map(r -> {
            RestaurantDTO dto = new RestaurantDTO();
            dto.setRstId(r.getRstId());
            dto.setName(r.getName());
            dto.setAddress(r.getAddress());
            dto.setIntro(r.getIntro());
            dto.setPhone(r.getPhone());
            dto.setImage(r.getImage());
            return dto;
        }).collect(Collectors.toList());
    }
    
    public Course getCourseEntityById(int id) {
        return courseRepository.findById(id)
               .orElseThrow(() -> new IllegalArgumentException("코스 없음"));
    }

}
