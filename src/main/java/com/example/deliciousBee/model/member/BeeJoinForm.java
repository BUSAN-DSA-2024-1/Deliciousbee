package com.example.deliciousBee.model.member;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class BeeJoinForm {
	@Size(min = 4, max = 20, message = "4~20자로 입력해주세요") //문자열의 길이 체크 Spring꺼
	private String member_id;    //아이디
	
	@Size(min = 4, max = 20, message = "4~20자로 입력해주세요") 
	private String password;     //패스워드
	
	@NotBlank(message = "정확한 이름을 입력해주세요") //null, 빈값, 공백 허용 x
	@Size(min = 2, max = 8, message = "2~8자로 입력해주세요")
	@Pattern(regexp = "\\S+", message = "공백을 포함할수없습니다")
	private String nickname;         //이름
	
	@NotNull(message = "성별을 선택해주세요")  //null 불가 but 빈값, 공백 허용
	private GenderType gender;       //성별
	
	@Past(message = "날짜가 잘못되었습니다") //과거만 선택하게해줌, *@PastOrPresent: 오늘을 포함한 과거날짜만, @Future: 미래만(영화예매)
	@NotNull(message = "날짜를 선택해주세요")                 
	private LocalDate birth;        //생년월일
	
	private String email;        //이메일

	private NationalType national;

	// 인증 번호와 토큰 추가
	private String verificationCode;
	private String token;

	//유효성검사다하면 Member객체로 변환해서 리턴
	public static BeeMember toMember(BeeJoinForm BeeJoinForm) {
		//MemberJoinForm 을 Member 객체로 변환하기
		//MemberRepository 가 <Member, String> 에서 Member를 받고있으니깐
		BeeMember member = new BeeMember();
		member.setMember_id(BeeJoinForm.getMember_id());
		member.setPassword(BeeJoinForm.getPassword());
		member.setNickname(BeeJoinForm.getNickname());
		member.setGender(BeeJoinForm.getGender());
		member.setNational(BeeJoinForm.getNational());
		member.setBirth(BeeJoinForm.getBirth());
		member.setEmail(BeeJoinForm.getEmail());
		
		return member;
	}
	

}