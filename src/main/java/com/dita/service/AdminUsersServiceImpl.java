package com.dita.service;

import com.dita.persistence.MemberRepository;
import com.dita.persistence.ReviewRepository;
import com.dita.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminUsersServiceImpl implements AdminUsersService {

    private final MemberRepository memberRepo;
    private final ReviewRepository reviewRepo;

    @Autowired
    public AdminUsersServiceImpl(MemberRepository memberRepo,
                                 ReviewRepository reviewRepo) {
        this.memberRepo = memberRepo;
        this.reviewRepo = reviewRepo;
    }

    @Override
    public Page<Member> listBySignup(LocalDateTime from, LocalDateTime to, Pageable p) {
        return memberRepo.findByCreateAtBetween(from, to, p);
    }

    @Override
    public Page<Member> listByReviewDate(LocalDateTime from, LocalDateTime to, Pageable p) {
        return memberRepo.findByReviewDateBetween(from, to, p);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        memberRepo.deleteAllById(ids);
    }

    @Override
    public long countReview(String memberId) {
        return reviewRepo.countByMemberMemberId(memberId);
    }
    
    @Override
    public Page<Member> listByRoleAndSignup(
        String role, LocalDateTime from, LocalDateTime to, Pageable p
    ) {
        return memberRepo.findByRoleAndCreateAtBetween(role, from, to, p);
    }
}
