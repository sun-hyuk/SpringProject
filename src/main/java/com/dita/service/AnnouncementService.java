package com.dita.service;

import com.dita.domain.Announcement;
import com.dita.dto.AnnouncementDTO;
import com.dita.persistence.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    // (파일 업로드 경로 등 기존 코드 그대로 두기)
    private String fileUploadPath = "C:/eatoMeter/uploads";

    /**
     * 모든 공지를 최신순으로 조회해서 AnnouncementDTO 리스트로 반환
     */
    public List<AnnouncementDTO> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        return announcements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ID로 공지 조회 + 조회수 증가
     */
    @Transactional
    public AnnouncementDTO getAnnouncementById(Long id) {
        Optional<Announcement> optional = announcementRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        announcementRepository.increaseViewCount(id);
        Announcement announcement = optional.get();
        announcement.setViews(announcement.getViews() != null ? announcement.getViews() + 1 : 1);

        return convertToDTO(announcement);
    }

    /**
     * 공지 저장 (신규/수정)
     */
    @Transactional
    public AnnouncementDTO saveAnnouncement(AnnouncementDTO announcementDTO) {
        Announcement announcement;

        if (announcementDTO.getAnnouncementId() != null) {
            Optional<Announcement> optional =
                announcementRepository.findById(announcementDTO.getAnnouncementId());
            if (optional.isPresent()) {
                announcement = optional.get();
            } else {
                throw new IllegalArgumentException(
                    "수정할 공지가 존재하지 않습니다. ID=" + announcementDTO.getAnnouncementId()
                );
            }
        } else {
            announcement = new Announcement();
            Long maxId = announcementRepository.findMaxAnnouncementId();
            announcement.setAnnouncementId(maxId + 1);
        }

        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        announcement.setAnnouncementType(announcementDTO.getAnnouncementType());
        announcement.setAdminId(announcementDTO.getAdminId());
        if (announcementDTO.getViews() != null) {
            announcement.setViews(announcementDTO.getViews());
        }

        MultipartFile file = announcementDTO.getFile();
        if (file != null && !file.isEmpty()) {
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            // ✅ 서버 파일 시스템 경로 (저장용)
            String fullPath = fileUploadPath + File.separator + uniqueFileName;

            // ✅ 웹 경로 (브라우저가 접근하는 경로로 저장)
            String webPath = "/uploads/" + uniqueFileName;
            try {
                File uploadDir = new File(fileUploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                file.transferTo(new File(fullPath));
                announcement.setFileName(file.getOriginalFilename());
                announcement.setFilePath(webPath);
            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        Announcement saved = announcementRepository.save(announcement);
        return convertToDTO(saved);
    }

    /**
     * 페이징 + 검색 + 유형 필터 처리된 공지목록 조회
     */
    public Page<AnnouncementDTO> getPagedAnnouncements(
            int page,
            int size,
            String type,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Announcement> entityPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            if (type == null || type.trim().isEmpty()) {
                entityPage = announcementRepository.findAll(pageable);
            } else {
                entityPage = announcementRepository.findByAnnouncementTypeOrderByCreatedAtDesc(type, pageable);
            }
        } else {
            String kw = keyword.trim();
            if (type == null || type.trim().isEmpty()) {
                entityPage = announcementRepository.findByTitleOrContentLike(kw, pageable);
            } else {
                entityPage = announcementRepository
                        .findByTypeAndTitleOrContentLike(type, kw, pageable);
            }
        }

        return entityPage.map(this::convertToDTO);
    }

    /**
     * 공지 삭제
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        Optional<Announcement> optional = announcementRepository.findById(id);
        if (optional.isPresent()) {
            Announcement announcement = optional.get();
            if (announcement.getFilePath() != null) {
                File file = new File(announcement.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
            }
            announcementRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("삭제할 공지가 존재하지 않습니다. ID=" + id);
        }
    }

    /**
     * Announcement → AnnouncementDTO 변환 헬퍼
     */
    private AnnouncementDTO convertToDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setAnnouncementId(announcement.getAnnouncementId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setAnnouncementType(announcement.getAnnouncementType());
        dto.setAdminId(announcement.getAdminId());
        dto.setFileName(announcement.getFileName());
        dto.setFilePath(announcement.getFilePath());
        dto.setCreatedAt(announcement.getCreatedAt());
        dto.setUpdatedAt(announcement.getUpdatedAt());
        dto.setViews(announcement.getViews());
        return dto;
    }
}
