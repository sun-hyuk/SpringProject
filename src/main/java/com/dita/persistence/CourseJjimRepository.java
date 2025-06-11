package com.dita.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dita.domain.CourseJjim;
import com.dita.domain.Member;
import com.dita.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseJjimRepository extends JpaRepository<CourseJjim, Integer> {

    // 특정 회원이 특정 코스를 찜했는지 확인
    Optional<CourseJjim> findByMemberAndCourse(Member member, Course course);

    // 찜 수 계산용 (선택)
    int countByCourse(Course course);

    // 찜 삭제
    void deleteByMemberAndCourse(Member member, Course course);
    
    @Query("SELECT cj.course FROM CourseJjim cj WHERE cj.member.memberId = :memberId ORDER BY cj.createAt DESC")
    List<Course> findByMemberIdWithPaging(@Param("memberId") String memberId, Pageable pageable);


}
