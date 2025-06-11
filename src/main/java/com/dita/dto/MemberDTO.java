package com.dita.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
	private String memberId;
    private String pwd;
    private String name;
    private String phone;
    private String nickname;
    private String image;
    private String role;
    private LocalDateTime createAt;
    private int reportScore;
    private LocalDate suspendedUntil;
 
}
