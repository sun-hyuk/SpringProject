package com.dita.dto;

import java.util.Date;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AnnouncementDTO {
    private Long announcementId;
    private String adminId;
    private String announcementType;
    private String content;
    private Date createdAt;
    private String fileName;
    private String filePath;
    private String title;
    private Date updatedAt;
    private Integer views;
    private MultipartFile file; // 파일 업로드용 필드
}
