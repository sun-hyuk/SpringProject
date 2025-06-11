package com.dita.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    private final String uploadDir = "/your/upload/path"; // 절대경로 또는 상대경로

    public String saveFile(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File targetFile = new File(uploadDir, filename);
            file.transferTo(targetFile);
            return "/upload/" + filename;
        } catch (Exception e) {
            log.error("파일 저장 실패", e);
            return null;
        }
    }

    public BigDecimal extractLatitude(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null && gps.getGeoLocation() != null) {
                return BigDecimal.valueOf(gps.getGeoLocation().getLatitude());
            }
        } catch (Exception e) {
            log.error("위도 추출 실패", e);
        }
        return null;
    }

    public BigDecimal extractLongitude(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null && gps.getGeoLocation() != null) {
                return BigDecimal.valueOf(gps.getGeoLocation().getLongitude());
            }
        } catch (Exception e) {
            log.error("경도 추출 실패", e);
        }
        return null;
    }
}
