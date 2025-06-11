package com.dita.controller;

import com.dita.domain.Member;
import com.dita.service.AdminUsersService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.*;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {
	
	
    private final AdminUsersService svc;
    private static final int PAGE_SIZE = 10;

    public AdminUsersController(AdminUsersService svc) {
        this.svc = svc;
    }

    @GetMapping
    public String list(
        @RequestParam(value="role",      required=false) String role,
        @RequestParam(value="startDate", required=false) String startStr,
        @RequestParam(value="endDate",   required=false) String endStr,
        @RequestParam(value="page",      defaultValue="1")    int page,
        Model model
    ) {
        LocalDate today = LocalDate.now();
        LocalDate s = (startStr != null && !startStr.isBlank())
                    ? LocalDate.parse(startStr) : today.minusMonths(1);
        LocalDate e = (endStr   != null && !endStr.isBlank())
                    ? LocalDate.parse(endStr)   : today;
        LocalDateTime from = s.atStartOfDay();
        LocalDateTime to   = e.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(page-1, PAGE_SIZE,
            Sort.by("createAt").descending());

        Page<Member> pg;
        if (role != null && !role.isEmpty()) {
            // 등급 + 기간
            pg = svc.listByRoleAndSignup(role, from, to, pageable);
        } else {
            // 기간만
            pg = svc.listBySignup(from, to, pageable);
        }

        // 리뷰 개수 채워주기
        pg.forEach(m -> m.setReviewCount(svc.countReview(m.getMemberId())));

        model.addAttribute("members",    pg.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  pg.getTotalPages());
        model.addAttribute("startPage",   Math.max(1, page-2));
        model.addAttribute("endPage",     Math.min(pg.getTotalPages(), page+2));
        model.addAttribute("today",       today);
        model.addAttribute("startDate",   s);
        model.addAttribute("endDate",     e);
        model.addAttribute("role",        role);

        return "admin/adminUsers";
    }


    @PostMapping
    public String delete(
        @RequestParam(value="member_id", required=false) List<String> ids,
        RedirectAttributes rt
    ) {
        if (ids != null && !ids.isEmpty()) {
            svc.deleteByIds(ids);
            rt.addFlashAttribute("deleted", true);
        }
        return "redirect:/admin/users";
    }
}
