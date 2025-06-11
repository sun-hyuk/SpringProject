package com.dita.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.dita.domain.Course;
import com.dita.domain.CourseJjim;
import com.dita.domain.Member;
import com.dita.dto.CourseDTO;
import com.dita.persistence.CourseJjimRepository;
import com.dita.persistence.CourseRepository;
import com.dita.persistence.MemberRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CourseJjimService {

    private final CourseJjimRepository jjimRepository;
    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    public CourseJjimService(CourseJjimRepository jjimRepository, 
                             CourseRepository courseRepository,
                             MemberRepository memberRepository) {
        this.jjimRepository = jjimRepository;
        this.courseRepository = courseRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void addJjim(int courseId, String memberId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스 없음: id=" + courseId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음: id=" + memberId));

        // 중복 체크
        Optional<CourseJjim> existing = jjimRepository.findByMemberAndCourse(member, course);
        if (existing.isEmpty()) {
            CourseJjim jjim = new CourseJjim();
            jjim.setMember(member);
            jjim.setCourse(course);
            jjimRepository.save(jjim);
        }
    }

    @Transactional
    public void removeJjim(int courseId, String memberId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스 없음: id=" + courseId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음: id=" + memberId));

        jjimRepository.deleteByMemberAndCourse(member, course);
    }

    public boolean isJjimmed(int courseId, String memberId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow();
        Member member = memberRepository.findById(memberId)
                .orElseThrow();
        return jjimRepository.findByMemberAndCourse(member, course).isPresent();
    }

    public int getJjimCount(int courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow();
        return jjimRepository.countByCourse(course);
    }
    
    public List<CourseDTO> getAllCourseDTOsWithJjim(String memberId) {
        List<Course> courseList = courseRepository.findAll();
        Member member = memberRepository.findById(memberId)
            .orElse(null); // 비로그인 시 null

        return courseList.stream().map(course -> {
            boolean isJjimmed = false;
            if (member != null) {
                isJjimmed = jjimRepository
                    .findByMemberAndCourse(member, course).isPresent();
            }
            return new CourseDTO(
                course.getCourse_id(), course.getTitle(), course.getIntro(), course.getTag(),
                course.getRestaurants(), course.getJjim_count(), course.getLikes(),
                course.getImage(), course.getCreate_at().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")),
                isJjimmed // ➕
            );
        }).toList();
    }
    
    public List<Course> getTop3JjimCourses(String memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) return List.of();

        List<CourseJjim> allJjims = jjimRepository.findAll();
        System.out.println(">> 전체 찜 개수: " + allJjims.size());

        List<Course> result = allJjims.stream()
                .filter(jjim -> jjim.getMember().equals(member))
                .sorted((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()))
                .map(CourseJjim::getCourse)
                .limit(3)
                .toList();

        System.out.println(">> 필터링된 코스 개수: " + result.size());
        return result;
    }
    
    public List<Course> getTopAllJjimCourses(String memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) return List.of();

        return jjimRepository.findAll().stream()
                .filter(jjim -> jjim.getMember().equals(member))
                .sorted((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()))
                .map(CourseJjim::getCourse)
                .toList();
    }
    
    public List<Course> getTopAllJjimCourses(String memberId, int page) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createAt").descending());

        return jjimRepository.findByMemberIdWithPaging(memberId, pageable);
    }
    
    public int countByCourse(int courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("코스 없음"));
        return jjimRepository.countByCourse(course);
    }
    
 // 찜 토글: 있으면 삭제, 없으면 추가
    public boolean toggleJjim(int courseId, String memberId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("코스 없음"));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        Optional<CourseJjim> existing = jjimRepository.findByMemberAndCourse(member, course);
        if (existing.isPresent()) {
        	jjimRepository.delete(existing.get());
            return false; // 찜 제거됨
        } else {
            CourseJjim jjim = new CourseJjim();
            jjim.setCourse(course);
            jjim.setMember(member);
            jjimRepository.save(jjim);
            return true; // 찜 추가됨
        }
    }

}
