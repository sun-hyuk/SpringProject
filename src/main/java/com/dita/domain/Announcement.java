package com.dita.domain;

import java.util.Date;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "announcement")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Announcement {

    @Id
    @Column(name = "announcement_id")
    private Long announcementId;    // ▶ @GeneratedValue 제거! 서비스에서 직접 세팅

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "announcement_type", nullable = false, length = 20)
    private String announcementType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "views")
    private Integer views = 0;

    // 테이블 컬럼명이 adminId(카멜케이스)이므로 그대로 매핑
    @Column(name = "adminId", length = 50)
    private String adminId;

    // DB에 저장되지 않는 필드
    @Transient
    private MultipartFile file;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}
