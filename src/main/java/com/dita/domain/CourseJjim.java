package com.dita.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_jjim")
@Getter
@Setter
@NoArgsConstructor
public class CourseJjim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_jjim_id")
    private Integer courseJjimId;

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createAt;

    // Member 테이블과 다대일(ManyToOne) 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    private Member member;

    // Course 테이블과 다대일(ManyToOne) 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;
}
