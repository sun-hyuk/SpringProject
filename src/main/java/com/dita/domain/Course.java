package com.dita.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int course_id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 255)
    private String intro;

    @Column(nullable = false, length = 255)
    private String tag;

    @Column(nullable = false, length = 255)
    private String restaurants;

    @Column(nullable = true)
    private int jjim_count = 0;

    @Column(nullable = true)
    private int likes = 0;

    @Column(nullable = false, length = 500)
    private String image;

    @Column(nullable = true)
    private LocalDateTime create_at = LocalDateTime.now();
}
