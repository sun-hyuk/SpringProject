package com.dita.controller;

import com.dita.domain.Course;
import com.dita.dto.MemberDTO;
import com.dita.dto.RstJjimDTO;
import com.dita.service.CourseJjimService;
import com.dita.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/myFavorites")
public class MyFavoritesController {

    private static final Logger logger = LoggerFactory.getLogger(MyFavoritesController.class);
    private final FavoriteService favoriteService;
    private final CourseJjimService courseJjimService;

    public MyFavoritesController(FavoriteService favoriteService,
                                 CourseJjimService courseJjimService) {
        this.favoriteService = favoriteService;
        this.courseJjimService = courseJjimService;
    }

    @GetMapping
    public String myFavoritesPage(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/login";

        String memberId = member.getMemberId();

        // 맛집 리스트
        List<RstJjimDTO> favoriteList = favoriteService.getMyFavoriteList(memberId, 0);
        model.addAttribute("favoriteList", favoriteList);
        model.addAttribute("favoriteCount", favoriteList.size());

        // 코스 리스트
        List<Course> courseList = courseJjimService.getTopAllJjimCourses(memberId, 0);
        model.addAttribute("courseList", courseList);
        model.addAttribute("courseCount", courseList.size());

        model.addAttribute("loggedInMember", member);
        return "myFavorites";
    }

    /**
     * 3) 찜 토글
     */
    @PostMapping("/toggle")
    @ResponseBody
    public String toggleFavorite(@RequestParam("rstId") Integer rstId, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            logger.info(">> [MyFavoritesController] /myFavorites/toggle – 비로그인 상태");
            return "not-logged-in";
        }

        String memberId = member.getMemberId();
        logger.info(">> [MyFavoritesController] /myFavorites/toggle – memberId={}, rstId={}", memberId, rstId);

        String addResult = favoriteService.addFavorite(memberId, rstId);
        if ("already".equals(addResult)) {
            String removeResult = favoriteService.removeFavorite(memberId, rstId);
            logger.info(">> [MyFavoritesController] removeFavorite 결과: {}", removeResult);
            return removeResult;
        }

        logger.info(">> [MyFavoritesController] addFavorite 결과: {}", addResult);
        return addResult;
    }

    
}
