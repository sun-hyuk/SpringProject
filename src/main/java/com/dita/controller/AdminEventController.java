package com.dita.controller;

import com.dita.domain.Event;
import com.dita.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class AdminEventController {

    @Autowired
    private EventService eventService;

    @Value("${file.upload-dir:C:/SpringBoot/EatoMeter/src/main/webapp/uploads/event}")
    private String uploadDir;

    // 이벤트 등록 폼 GET 요청 처리
    @GetMapping("/adminEvent")
    public String showEventForm() {
        return "admin/adminEvent";  // HTML 또는 JSP 파일 경로
    }
    
    @PostMapping("/adminEvent/register")
    public String registerEvent(@RequestParam("title") String title,
                                @RequestParam("content") String content,
                                @RequestParam("startDate") String startDate,
                                @RequestParam("endDate") String endDate,
                                @RequestParam("image") MultipartFile image,
                                RedirectAttributes redirectAttributes) {
        try {
        	// 디렉토리 존재 여부 확인 및 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 1. 이미지 저장
            String filename = null;
            if (!image.isEmpty()) {
                String originalName = image.getOriginalFilename();
                String ext = originalName.substring(originalName.lastIndexOf("."));
                filename = UUID.randomUUID().toString() + ext;
                File dest = new File(uploadDir, filename);
                image.transferTo(dest);
            }

            // 2. Event 객체 생성
            Event event = new Event();
            event.setAdminId("admin1");  // 세션에서 가져오는 것이 이상적이나 임시 고정
            event.setTitle(title);
            event.setContent(content);
            event.setStartDate(LocalDate.parse(startDate));
            event.setEndDate(LocalDate.parse(endDate));
            event.setImageUrl("/uploads/event/" + filename);
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());
            event.setViews(0);
            event.setStatus("진행중");

            // 3. 저장
            eventService.saveEvent(event);

            redirectAttributes.addFlashAttribute("msg", "이벤트 등록 완료");
            return "redirect:/adminEvent";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg", "이벤트 등록 실패");
            return "redirect:/adminEvent";
        }
    }
}
