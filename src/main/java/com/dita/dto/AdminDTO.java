package com.dita.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
	private String adminId;
    private String adminPwd;
    private String adminNickname;
 
}
