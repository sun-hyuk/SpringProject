package com.dita.service;

import com.dita.domain.Member;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminUsersService {
    Page<Member> listBySignup(LocalDateTime from, LocalDateTime to, Pageable p);
    
    Page<Member> listByReviewDate(LocalDateTime from, LocalDateTime to, Pageable p);
    void deleteByIds(List<String> ids);
    long countReview(String memberId);
    
    Page<Member> listByRoleAndSignup(
            String role, LocalDateTime from, LocalDateTime to, Pageable p
        );
}
