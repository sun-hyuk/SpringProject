package com.dita.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dita.domain.Member;
import com.dita.domain.Restaurant;
import com.dita.domain.RstJjim;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.RestaurantRepository;
import com.dita.persistence.RstJjimRepository;

import jakarta.transaction.Transactional;

@Service
public class RstJjimService {

    @Autowired
    private RstJjimRepository rstJjimRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;

    // ✅ 찜 여부 확인
    public boolean isJjim(String memberId, int rstId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Restaurant restaurant = restaurantRepository.findById(rstId).orElseThrow();
        return rstJjimRepository.findByMemberAndRestaurant(member, restaurant).isPresent();
    }

    // ✅ 찜 토글 처리
    @Transactional
    public boolean toggleJjim(String memberId, int rstId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Restaurant restaurant = restaurantRepository.findById(rstId).orElseThrow();

        Optional<RstJjim> existing = rstJjimRepository.findByMemberAndRestaurant(member, restaurant);
        if (existing.isPresent()) {
            rstJjimRepository.delete(existing.get());
            restaurantRepository.decrementJjimCount(rstId);
            return false;
        } else {
            RstJjim jjim = new RstJjim();
            jjim.setMember(member); // ⬅️ 객체 연관 설정
            jjim.setRestaurant(restaurant);
            // createdAt은 DB에서 자동 설정되므로 생략 가능하지만, 직접 넣어도 OK
            jjim.setCreatedAt(LocalDateTime.now()); 
            rstJjimRepository.save(jjim);
            restaurantRepository.incrementJjimCount(rstId);
            return true;
        }
    }
    
    // ★ 회원이 찜한 전체 리스트 조회
    public List<RstJjim> getJjimsByMember(String memberId) {
        return rstJjimRepository.findByMember_MemberId(memberId);
    }

    // ★ 식당별 찜(북마크) 카운트 조회
    public int countJjim(Integer rstId) {
        return rstJjimRepository.countByRestaurantRstId(rstId);
    }
}
