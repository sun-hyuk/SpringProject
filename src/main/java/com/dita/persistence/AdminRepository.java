package com.dita.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dita.domain.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {
    /**
     * adminId 와 adminPwd 가 일치하는 엔티티를 Optional 로 반환
     */
    Optional<Admin> findByAdminIdAndAdminPwd(String adminId, String adminPwd);

    /**
     * (선택) adminId 만으로 조회하는 메서드도 정의해 두면, 
     * 로그인 실패 시 “존재하지 않는 ID” vs “비밀번호 틀림”을 구분하고 싶을 때 유용합니다.
     */
    /* Optional<Admin> findByAdminId(String adminId); */
}
