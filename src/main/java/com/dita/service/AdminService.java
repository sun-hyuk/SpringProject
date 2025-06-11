package com.dita.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dita.domain.Admin;
import com.dita.dto.AdminDTO;
import com.dita.persistence.AdminRepository;

@Service
public class AdminService {

	private final AdminRepository adminRepo;

	@Autowired
	public AdminService(AdminRepository adminRepo) {
		this.adminRepo = adminRepo;
	}

	/**
	 * 관리자 로그인 검증
	 * 
	 * @param id  관리자 아이디
	 * @param pwd 관리자 비밀번호
	 * @return 일치하는 관리자가 존재하면 true, 아니면 false
	 */
	public boolean loginAdmin(String id, String pwd) {
		return adminRepo.findByAdminIdAndAdminPwd(id, pwd).isPresent();
	}

	/**
	 * 로그인 성공 시 AdminDTO를 반환
	 */
	public Optional<AdminDTO> findByCredentials(String id, String pwd) {
		Optional<Admin> opt = adminRepo.findByAdminIdAndAdminPwd(id, pwd);
		if (opt.isEmpty())
			return Optional.empty();

		Admin a = opt.get();
		AdminDTO dto = new AdminDTO(a.getAdminId(), a.getAdminPwd(), a.getAdminNickname());
		return Optional.of(dto);
	}

	/**
	 * 엔티티 → DTO 변환 헬퍼
	 */
	private AdminDTO toDto(Admin admin) {
		AdminDTO dto = new AdminDTO();
		dto.setAdminId(admin.getAdminId());
		dto.setAdminPwd(admin.getAdminPwd());
		dto.setAdminNickname(admin.getAdminNickname());
		return dto;
	}
}
