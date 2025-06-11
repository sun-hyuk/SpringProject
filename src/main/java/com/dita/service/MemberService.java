package com.dita.service;

import com.dita.domain.Member;
import com.dita.dto.MemberDTO;
import com.dita.persistence.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class MemberService {

    private final MemberRepository memberRepo;
    private final JavaMailSender mailSender;

    @Autowired
    public MemberService(MemberRepository memberRepo, JavaMailSender mailSender) {
        this.memberRepo = memberRepo;
        this.mailSender = mailSender;
    }

    /**
     * 일반 회원 로그인 검증 (아이디/비밀번호 일치 여부)
     */
    public boolean loginMember(String id, String pwd) {
        return memberRepo.findByMemberIdAndPwd(id, pwd).isPresent();
    }

    /**
     * 로그인 성공 시 DTO 형태로 회원 정보를 가져옴
     */
    public Optional<MemberDTO> findMemberByCredentials(String id, String pwd) {
        Optional<Member> opt = memberRepo.findByMemberIdAndPwd(id, pwd);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Member m = opt.get();
        MemberDTO dto = toDto(m);
        return Optional.of(dto);
    }

    /**
     * 회원가입 (DTO → 엔티티 → 저장 → DTO 반환)
     */
    public Optional<MemberDTO> registerMember(MemberDTO memberDto) {
        // 1) 동일한 memberId가 이미 있는지 확인
        if (memberRepo.existsById(memberDto.getMemberId())) {
            return Optional.empty();
        }

        // 2) DTO → 엔티티 변환
        Member member = new Member();
        member.setMemberId(memberDto.getMemberId());
        member.setPwd(memberDto.getPwd());
        member.setName(memberDto.getName());
        member.setPhone(memberDto.getPhone());
        member.setNickname(memberDto.getNickname());
        // DTO에 image 필드가 있다면 초기값 설정(예: null)
        member.setImage(memberDto.getImage());

        // 생성일자, 기본 역할 등은 서비스에서 설정
        member.setCreateAt(LocalDateTime.now());
        member.setRole("user");
        member.setReportScore(0);
        member.setSuspendedUntil(null);

        Member saved = memberRepo.save(member);

        // 3) 저장된 엔티티 → DTO로 변환해서 반환
        MemberDTO savedDto = toDto(saved);
        return Optional.of(savedDto);
    }

    // --- 기존 기능은 그대로 두고, 아래에 업데이트 기능 추가 ---

    /**
     * (1) 회원 닉네임 업데이트
     *
     * @param memberId 수정할 회원 ID
     * @param nickname 새 닉네임
     */
    public void updateNickname(String memberId, String nickname) {
        Member entity = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID: " + memberId));
        entity.setNickname(nickname);
        memberRepo.save(entity);
    }
    
    /**
     * (1) 현재 비밀번호 일치 여부 확인
     */
    public boolean checkPassword(String memberId, String currentPwd) {
        // DB에서 memberId와 currentPwd가 일치하는 엔티티가 있는지 확인
        return memberRepo.findByMemberIdAndPwd(memberId, currentPwd).isPresent();
    }

    /**
     * (2) 비밀번호 업데이트
     */
    public void updatePassword(String memberId, String newPwd) {
        Member entity = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID: " + memberId));
        entity.setPwd(newPwd);
        memberRepo.save(entity);
    }
    
    /**
     * 프로필 이미지 경로 업데이트
     * @param memberId    회원 ID
     * @param imagePath   업로드된 이미지의 상대 경로 (예: "/uploads/profiles/uuid.png")
     */
    public void updateProfileImage(String memberId, String imagePath) {
        Member entity = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID: " + memberId));
        entity.setImage(imagePath);
        memberRepo.save(entity);
    }
    
    /**
     * 회원 탈퇴: memberId로 회원 조회 후 삭제
     * @param memberId 삭제 대상 회원 ID
     */
    public void deleteMemberById(String memberId) {
        Member entity = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID: " + memberId));
        memberRepo.delete(entity);
    }

    /**
     * (2) memberId로 회원 전체 정보 조회 (엔티티 → DTO)
     *
     * @param memberId 회원 ID
     * @return MemberDTO (없는 경우 IllegalArgumentException 발생)
     */
    public MemberDTO findByMemberId(String memberId) {
        Member entity = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID: " + memberId));
        return toDto(entity);
    }

    /**
     * Member 엔티티 → MemberDTO 변환 헬퍼
     */
    private MemberDTO toDto(Member m) {
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(m.getMemberId());
        // 패스워드는 보통 응답에 보내지 않지만, 예시로 그대로 복사
        dto.setPwd(m.getPwd());
        dto.setName(m.getName());
        dto.setPhone(m.getPhone());
        dto.setNickname(m.getNickname());
        dto.setImage(m.getImage());           // 엔티티의 image 필드도 DTO에 복사
        dto.setRole(m.getRole());
        dto.setCreateAt(m.getCreateAt());
        dto.setReportScore(m.getReportScore());
        dto.setSuspendedUntil(m.getSuspendedUntil());
        return dto;
    }
    
    // ✅ [추가된 기능 - 아이디 마스킹 찾기]
	    public String findMaskedMemberId(String name, String phone) {
	        return memberRepo.findByNameAndPhone(name, phone)
	                .map(Member::getMemberId)
	                .map(this::maskEmail)
	                .orElse(null);
	    }

	    private String maskEmail(String email) {
	        int atIndex = email.indexOf("@");
	        if (atIndex > 2) {
	            return email.substring(0, 3) + "****" + email.substring(atIndex);
	        } else {
	            return "****" + email.substring(atIndex);
	        }
	    }

	    // ✅ [추가된 기능 - 임시 비밀번호 발급 및 메일 전송]
	    public boolean sendTemporaryPassword(String memberId, String phone) {
	        Optional<Member> opt = memberRepo.findByMemberIdAndPhone(memberId, phone);
	        if (opt.isPresent()) {
	            Member member = opt.get();
	            String tempPwd = generateTempPassword();
	            member.setPwd(tempPwd); // 실서비스에서는 암호화 추천
	            memberRepo.save(member);

	            sendEmail(member.getMemberId(), tempPwd); // memberId 자체가 이메일
	            return true;
	        }
	        return false;
	    }

	    private void sendEmail(String to, String tempPwd) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(to);
	        message.setSubject("[EatoMeter] 임시 비밀번호 안내");
	        message.setText("임시 비밀번호: " + tempPwd + "\n로그인 후 반드시 비밀번호를 변경해주세요.");
	        mailSender.send(message);
	    }

	    private String generateTempPassword() {
	        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	        StringBuilder sb = new StringBuilder();
	        Random rnd = new Random();
	        for (int i = 0; i < 8; i++) {
	            sb.append(chars.charAt(rnd.nextInt(chars.length())));
	        }
	        return sb.toString();
	    }
}
