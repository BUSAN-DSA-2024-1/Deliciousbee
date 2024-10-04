package com.example.deliciousBee.model.member;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class PasswordChange {
	@Size(min = 4, max = 20) // 4~20자리 제한
	@Column(length = 20, nullable = false) // 꼭 적어야된다
	private String password;     //패스워드

	private String confirmPassword; //패스워드 확인
	
	public static BeeMember toBeeMember(PasswordChange passwordChange) {
		BeeMember member = new BeeMember();

		member.setPassword(passwordChange.getPassword());

		return member;
	}
}
