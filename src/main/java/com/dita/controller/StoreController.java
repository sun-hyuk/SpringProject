package com.dita.controller;

import com.dita.domain.Restaurant;
import com.dita.dto.MemberDTO;
import com.dita.service.RestaurantService;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("")
public class StoreController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping("/storeRegister")
    public String showRegisterPage(HttpSession session, Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");

        if (memberDto == null) {
            return "redirect:/login"; // 비로그인 시 접근 차단
        }

        model.addAttribute("loggedInMember", memberDto); // 뷰에서 사용자 정보 사용 가능
        return "store/storeRegister"; // templates/store/storeRegister.html
    }

    @PostMapping("/storeRegister")
    public String registerStore(@ModelAttribute Restaurant restaurant,
                                 @RequestParam("mainImage") MultipartFile file,
                                 @RequestParam("city") String city,
                                 @RequestParam("district") String district,
                                 @RequestParam("roadAddress") String roadAddress,
                                 HttpSession session) throws IOException {

        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto == null) {
            return "redirect:/login";
        }

        // 사용자 정보 설정
        restaurant.setMemberId(memberDto.getMemberId());
        restaurant.setAddress(roadAddress);

        // ================================
        // 1. 파일 저장 처리
        // ================================
        if (!file.isEmpty()) {
            // 저장할 폴더 (정적 경로)
        	String uploadDir = "src/main/webapp/uploads/rst";

            // 저장할 파일명 (UUID로 중복 방지)
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 폴더 없으면 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 저장 경로 지정
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // DB에는 웹에서 접근 가능한 상대 경로 저장
            restaurant.setImage("/uploads/rst/" + fileName);
        } else {
            // 파일이 없으면 기본 이미지로 설정하거나 null 처리
            restaurant.setImage("/images/default.png");
        }

        // ================================
        // 2. 지역 및 상태 설정
        // ================================
        restaurant.setRegionLabel(city);
        restaurant.setRegion2Label(district);
        restaurant.setStatus("대기");

        // ================================
        // 3. 저장
        // ================================
        restaurantService.save(restaurant);

        return "redirect:/store/complete";
    }
    
    @GetMapping("/store/complete")
    public String showCompletePage(@RequestParam(value = "type", required = false) String type,
            Model model) {
		model.addAttribute("type", type);
		return "store/complete";
	}

    @GetMapping("/storeManage")
    public String showStoreManagePage(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/login";

        List<Restaurant> approvedList = restaurantService.findApprovedStoresByMemberId(member.getMemberId());
        List<Restaurant> pendingList = restaurantService.findPendingStoresByMemberId(member.getMemberId());

        model.addAttribute("approvedList", approvedList);
        model.addAttribute("pendingList", pendingList);
        model.addAttribute("loggedInMember", member);
        return "store/storeManage";
    }
    
    @GetMapping("/store/storeModify")
    public String showModifyPage(@RequestParam("rstId") int rstId, Model model, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/login";

        Restaurant store = restaurantService.findById(rstId);
        if (store == null || !store.getMemberId().equals(member.getMemberId())) {
            return "redirect:/store/manage"; // 잘못된 접근 방지
        }

        model.addAttribute("store", store);
        return "store/storeModify";
    }
    
    @PostMapping("/store/update")
    public String updateStore(@ModelAttribute Restaurant restaurant, @RequestParam("mainImage") MultipartFile file, HttpSession session) throws IOException {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/login";

        // 기존 이미지 유지
        Restaurant existing = restaurantService.findById(restaurant.getRstId());
        if (existing == null) return "redirect:/error";

        // 이미지가 새로 업로드된 경우
        if (!file.isEmpty()) {
        	String uploadDir = "src/main/webapp/uploads/rst";
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            restaurant.setImage("/uploads/rst/" + fileName); // 웹 접근 경로
        } else {
            restaurant.setImage(existing.getImage()); // 기존 이미지 유지
        }

        restaurant.setMemberId(member.getMemberId()); // 소유자 유지
        
        restaurantService.update(restaurant);
        return "redirect:/store/complete?type=update";
    }
}
